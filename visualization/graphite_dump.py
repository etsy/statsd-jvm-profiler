#!/usr/bin/env python
from optparse import OptionParser
import json
import urllib

def get_arg_parser():
    parser = OptionParser()
    parser.add_option('-o', '--host', dest='host', help='Hostname of graphite server', metavar='HOST')
    parser.add_option('-p', '--prefix', dest='prefix', help='Prefix of metric for which to dump the tree', metavar='PREFIX')
    parser.add_option('-s', '--start', dest='start', help='Start date for query', metavar='DATE')
    parser.add_option('-e', '--end', dest='end', help='End date for query', metavar='DATE')

    return parser

    
def get_children(host, prefix):
    url = 'http://%s/metrics/find?query=%s.*' % (host, prefix)
    json_url = urllib.urlopen(url)
    json_results = json.loads(json_url.read()) 
    leaves = []
    expandable = []

    for child in json_results:
        if child['leaf'] == 0:
            expandable.append(child['id'])
        else:
            leaves.append(child['id'])

    return (leaves, expandable)

    
def get_max_metric(host, metric, start, end):
    url = 'http://%s/render?target=keepLastValue(%s)&format=json&from=%s&until=%s' % (host, metric, start, end)
    json_url = urllib.urlopen(url)
    json_results = json.loads(json_url.read())
    return max([point[0] for point in json_results[0]['datapoints']])

    
def get_tree(host, prefix, start, end):
    nodes_to_process = [prefix]
    leaves = []
    while len(nodes_to_process) > 0:
        curr = nodes_to_process.pop()
        (l, e) = get_children(host, curr)
        nodes_to_process.extend(e)
        leaves.extend(l)

    results = {}
    for leaf in leaves:
        results[leaf] = get_max_metric(host, leaf, start, end)

    return results

    
def format_metric(metric, prefix):
    no_prefix = metric.replace(prefix + '.', '')
    frames = no_prefix.split('.')
    frames.reverse()
    return ';'.join(frames).replace('-', '.')

    
def format_output(prefix, results):
    for metric, value in results.iteritems():
        print '%s %d' % (format_metric(metric, prefix), value)

        
if __name__ == '__main__':
    parser = get_arg_parser()
    args, _ = parser.parse_args()
    if not(args.host and args.prefix and args.start and args.end):
        parser.print_help()
        sys.exit(255)

    results = get_tree(args.host, args.prefix, args.start, args.end)
    format_output(args.prefix, results)
