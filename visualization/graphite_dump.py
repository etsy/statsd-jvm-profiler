#!/usr/bin/env python
from optparse import OptionParser
import requests

def get_arg_parser():
    parser = OptionParser()
    parser.add_option('-o', '--host', dest='host', help='Hostname of graphite server', metavar='HOST')
    parser.add_option('-r', '--port', dest='port', help='Port for graphite server', metavar='PORT')
    parser.add_option('-p', '--prefix', dest='prefix', help='Prefix of metric for which to dump the tree', metavar='PREFIX')
    parser.add_option('-s', '--start', dest='start', help='Start date for query', metavar='DATE')
    parser.add_option('-e', '--end', dest='end', help='End date for query', metavar='DATE')

    return parser

    
def get_children(host, prefix, min, max):
    url = 'http://%s/metrics/expand' % host
    leaves = []
    for i in range(min, max + 1):
        params = {'query': '%s%s' % (prefix, '.*' * i), 'leavesOnly': '1'}
        json_url = requests.get(url, params=params)
        json_results = json_url.json()
        leaves.extend(json_results['results'])

    return leaves


def get_bounds(host, prefix):
    params = {'query': '%s.*' % prefix, 'leavesOnly': '1'}
    url = 'http://%s/metrics/expand' % host
    json_url = requests.get(url, params=params)
    json_results = json_url.json()
    bounds = []
    for bound in json_results['results']:
        boundresult = bound.replace(prefix + '.', '')
        if boundresult.isdigit():
            bounds.append(int(boundresult))

    return (min(bounds), max(bounds))

    
def get_max_metric(host, metric, start, end):
    params = {'target': 'keepLastValue(%s)' % metric, 'format': 'json', 'from': start, 'until': end}
    url = 'http://%s/render' % host
    json_url = requests.get(url, params=params)
    json_results = json_url.json()
    if not json_results:
        return None
    else:
        return max([point[0] for point in json_results[0]['datapoints']])

    
def get_tree(host, prefix, start, end):
    nodes_to_process = [prefix]
    (min, max) = get_bounds(host, prefix)
    leaves = get_children(host, prefix, min, max)

    results = {}
    for leaf in leaves:
        leafmetric = get_max_metric(host, leaf, start, end)
        if leafmetric is not None:
            results[leaf] = leafmetric

    return results

    
def format_metric(metric, prefix):
    no_prefix = metric.replace(prefix + '.', '')
    frames = no_prefix.split('.')
    frames.reverse()
    return ';'.join(frames).replace('-', '.')

    
def format_output(prefix, results):
    for metric, value in results.iteritems():
        if value is not None:
            print '%s %d' % (format_metric(metric, prefix), value)

        
if __name__ == '__main__':
    parser = get_arg_parser()
    args, _ = parser.parse_args()
    if not(args.host and args.prefix and args.start and args.end):
        parser.print_help()
        sys.exit(255)

    host = args.host
    if args.port is not None:
        host = '%s:%s' % (host, args.port)

    results = get_tree(host, args.prefix, args.start, args.end)
    format_output(args.prefix, results)
