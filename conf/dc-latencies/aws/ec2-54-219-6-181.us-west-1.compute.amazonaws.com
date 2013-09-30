PING ec2-54-227-42-64.compute-1.amazonaws.com (54.227.42.64) 56(84) bytes of data.
64 bytes from ec2-54-227-42-64.compute-1.amazonaws.com (54.227.42.64): icmp_req=1 ttl=44 time=106 ms
64 bytes from ec2-54-227-42-64.compute-1.amazonaws.com (54.227.42.64): icmp_req=2 ttl=44 time=83.8 ms
64 bytes from ec2-54-227-42-64.compute-1.amazonaws.com (54.227.42.64): icmp_req=3 ttl=44 time=83.8 ms
64 bytes from ec2-54-227-42-64.compute-1.amazonaws.com (54.227.42.64): icmp_req=4 ttl=44 time=84.3 ms
64 bytes from ec2-54-227-42-64.compute-1.amazonaws.com (54.227.42.64): icmp_req=5 ttl=44 time=99.9 ms
64 bytes from ec2-54-227-42-64.compute-1.amazonaws.com (54.227.42.64): icmp_req=6 ttl=44 time=115 ms
64 bytes from ec2-54-227-42-64.compute-1.amazonaws.com (54.227.42.64): icmp_req=7 ttl=44 time=84.0 ms
64 bytes from ec2-54-227-42-64.compute-1.amazonaws.com (54.227.42.64): icmp_req=8 ttl=44 time=121 ms
64 bytes from ec2-54-227-42-64.compute-1.amazonaws.com (54.227.42.64): icmp_req=9 ttl=44 time=103 ms
64 bytes from ec2-54-227-42-64.compute-1.amazonaws.com (54.227.42.64): icmp_req=10 ttl=44 time=84.3 ms

--- ec2-54-227-42-64.compute-1.amazonaws.com ping statistics ---
10 packets transmitted, 10 received, 0% packet loss, time 9013ms
rtt min/avg/max/mdev = 83.813/96.701/121.003/13.812 ms
PING ec2-54-219-6-181.us-west-1.compute.amazonaws.com (10.197.84.225) 56(84) bytes of data.
64 bytes from ip-10-197-84-225.us-west-1.compute.internal (10.197.84.225): icmp_req=1 ttl=64 time=0.028 ms
64 bytes from ip-10-197-84-225.us-west-1.compute.internal (10.197.84.225): icmp_req=2 ttl=64 time=0.054 ms
64 bytes from ip-10-197-84-225.us-west-1.compute.internal (10.197.84.225): icmp_req=3 ttl=64 time=0.056 ms
64 bytes from ip-10-197-84-225.us-west-1.compute.internal (10.197.84.225): icmp_req=4 ttl=64 time=0.056 ms
64 bytes from ip-10-197-84-225.us-west-1.compute.internal (10.197.84.225): icmp_req=5 ttl=64 time=0.056 ms
64 bytes from ip-10-197-84-225.us-west-1.compute.internal (10.197.84.225): icmp_req=6 ttl=64 time=0.056 ms
64 bytes from ip-10-197-84-225.us-west-1.compute.internal (10.197.84.225): icmp_req=7 ttl=64 time=0.058 ms
64 bytes from ip-10-197-84-225.us-west-1.compute.internal (10.197.84.225): icmp_req=8 ttl=64 time=0.061 ms
64 bytes from ip-10-197-84-225.us-west-1.compute.internal (10.197.84.225): icmp_req=9 ttl=64 time=0.056 ms
64 bytes from ip-10-197-84-225.us-west-1.compute.internal (10.197.84.225): icmp_req=10 ttl=64 time=0.059 ms

--- ec2-54-219-6-181.us-west-1.compute.amazonaws.com ping statistics ---
10 packets transmitted, 10 received, 0% packet loss, time 9011ms
rtt min/avg/max/mdev = 0.028/0.054/0.061/0.008 ms
PING ec2-54-229-197-90.eu-west-1.compute.amazonaws.com (54.229.197.90) 56(84) bytes of data.
64 bytes from ec2-54-229-197-90.eu-west-1.compute.amazonaws.com (54.229.197.90): icmp_req=1 ttl=47 time=154 ms
64 bytes from ec2-54-229-197-90.eu-west-1.compute.amazonaws.com (54.229.197.90): icmp_req=2 ttl=47 time=155 ms
64 bytes from ec2-54-229-197-90.eu-west-1.compute.amazonaws.com (54.229.197.90): icmp_req=3 ttl=47 time=154 ms
64 bytes from ec2-54-229-197-90.eu-west-1.compute.amazonaws.com (54.229.197.90): icmp_req=4 ttl=47 time=175 ms
64 bytes from ec2-54-229-197-90.eu-west-1.compute.amazonaws.com (54.229.197.90): icmp_req=5 ttl=47 time=154 ms
64 bytes from ec2-54-229-197-90.eu-west-1.compute.amazonaws.com (54.229.197.90): icmp_req=6 ttl=47 time=155 ms
64 bytes from ec2-54-229-197-90.eu-west-1.compute.amazonaws.com (54.229.197.90): icmp_req=7 ttl=47 time=154 ms
64 bytes from ec2-54-229-197-90.eu-west-1.compute.amazonaws.com (54.229.197.90): icmp_req=8 ttl=47 time=163 ms
64 bytes from ec2-54-229-197-90.eu-west-1.compute.amazonaws.com (54.229.197.90): icmp_req=9 ttl=47 time=188 ms
64 bytes from ec2-54-229-197-90.eu-west-1.compute.amazonaws.com (54.229.197.90): icmp_req=10 ttl=47 time=154 ms

--- ec2-54-229-197-90.eu-west-1.compute.amazonaws.com ping statistics ---
10 packets transmitted, 10 received, 0% packet loss, time 9013ms
rtt min/avg/max/mdev = 154.233/161.027/188.035/11.025 ms
PING ec2-54-238-139-0.ap-northeast-1.compute.amazonaws.com (54.238.139.0) 56(84) bytes of data.
64 bytes from ec2-54-238-139-0.ap-northeast-1.compute.amazonaws.com (54.238.139.0): icmp_req=1 ttl=46 time=153 ms
64 bytes from ec2-54-238-139-0.ap-northeast-1.compute.amazonaws.com (54.238.139.0): icmp_req=2 ttl=46 time=160 ms
64 bytes from ec2-54-238-139-0.ap-northeast-1.compute.amazonaws.com (54.238.139.0): icmp_req=3 ttl=46 time=137 ms
64 bytes from ec2-54-238-139-0.ap-northeast-1.compute.amazonaws.com (54.238.139.0): icmp_req=4 ttl=46 time=129 ms
64 bytes from ec2-54-238-139-0.ap-northeast-1.compute.amazonaws.com (54.238.139.0): icmp_req=5 ttl=46 time=149 ms
64 bytes from ec2-54-238-139-0.ap-northeast-1.compute.amazonaws.com (54.238.139.0): icmp_req=6 ttl=46 time=136 ms
64 bytes from ec2-54-238-139-0.ap-northeast-1.compute.amazonaws.com (54.238.139.0): icmp_req=7 ttl=46 time=138 ms
64 bytes from ec2-54-238-139-0.ap-northeast-1.compute.amazonaws.com (54.238.139.0): icmp_req=8 ttl=46 time=126 ms
64 bytes from ec2-54-238-139-0.ap-northeast-1.compute.amazonaws.com (54.238.139.0): icmp_req=9 ttl=46 time=140 ms
64 bytes from ec2-54-238-139-0.ap-northeast-1.compute.amazonaws.com (54.238.139.0): icmp_req=10 ttl=46 time=126 ms

--- ec2-54-238-139-0.ap-northeast-1.compute.amazonaws.com ping statistics ---
10 packets transmitted, 10 received, 0% packet loss, time 9011ms
rtt min/avg/max/mdev = 126.228/140.031/160.513/10.867 ms
