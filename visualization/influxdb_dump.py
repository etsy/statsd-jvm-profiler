#!/usr/bin/env python
from optparse import OptionParser
from influxdb import InfluxDBClient
import sys

class InfluxDBDump:
    def __init__(self, host, port, username, password, database, prefix, tag_mapping):
        self.host = host
        self.port = port
        self.username = username
        self.password = password
        self.database = database
        self.prefix = prefix
        self.tag_mapping = tag_mapping
        self.client = InfluxDBClient(self.host, self.port, self.username, self.password, self.database)
        self.mapped_tags = self._construct_tag_mapping(prefix, tag_mapping)

    def run(self):
        clauses = ["%s ='%s'" % (tag, value) for (tag, value) in self.mapped_tags.iteritems()]
        query = 'select value from /^cpu.trace.*/ where %s' % " and ".join(clauses)
        metrics = self.client.query(query).raw['series']
        for metric in metrics:
            name = self._format_metric_name(metric['name'], 'cpu.trace.')
            value = sum([v[1] for v in metric['values']])
            if name != str(value):
                print '%s %d' % (name, value)

    def _format_metric_name(self, name, prefix):
        tokens = name.replace(prefix, '').split('.')
        reverse = reversed(tokens)
        line_numbers = [':'.join(r.rsplit('-', 1)) for r in reverse]
        return ';'.join(line_numbers).replace('-', '.')

    def _construct_tag_mapping(self, prefix, tag_mapping):
        mapped_tags = {}
        if tag_mapping:
            tag_names = tag_mapping.split('.')
            prefix_components = prefix.split('.')
            if len(tag_names) != len(prefix_components):
                raise Exception('Invalid tag mapping %s' % tag_mapping)

            zipped = zip(tag_names, prefix_components)
            for entry in zipped:
                if entry[0] != 'SKIP':
                    mapped_tags[entry[0]] = entry[1]
        else:
            mapped_tags['prefix'] = prefix

        return mapped_tags


def get_arg_parser():
    parser = OptionParser()
    parser.add_option('-o', '--host', dest='host', help='Hostname of InfluxDB server', metavar='HOST')
    parser.add_option('-r', '--port', dest='port', help='Port for InfluxDB HTTP API (defaults to 8086)', metavar='PORT')
    parser.add_option('-u', '--username', dest='username', help='Username with which to connect to InfluxDB', metavar='USER')
    parser.add_option('-p', '--password', dest='password', help='Password with which to connect to InfluxDB', metavar='PASSWORD')
    parser.add_option('-d', '--database', dest='database', help='InfluxDB database which contains profiler data', metavar='DB')
    parser.add_option('-e', '--prefix', dest='prefix', help='Metric prefix', metavar='PREFIX')
    parser.add_option('-t', '--tag-mapping', dest='mapping', help='Tag mapping for metric prefix', metavar='MAPPING')

    return parser

if __name__ == '__main__':
    parser = get_arg_parser()
    args, _ = parser.parse_args()
    if not(args.host and args.username and args.password and args.database and args.prefix):
        parser.print_help()
        sys.exit(255)
    port = args.port or 8086
    tag_mapping = args.mapping or None
    dumper = InfluxDBDump(args.host, port, args.username, args.password, args.database, args.prefix, tag_mapping)
    dumper.run()

