PING ec2-54-227-42-64.compute-1.amazonaws.com (54.227.42.64) 56(84) bytes of data.
64 bytes from ec2-54-227-42-64.compute-1.amazonaws.com (54.227.42.64): icmp_req=1 ttl=45 time=97.6 ms
64 bytes from ec2-54-227-42-64.compute-1.amazonaws.com (54.227.42.64): icmp_req=2 ttl=45 time=98.6 ms
64 bytes from ec2-54-227-42-64.compute-1.amazonaws.com (54.227.42.64): icmp_req=3 ttl=45 time=97.7 ms
64 bytes from ec2-54-227-42-64.compute-1.amazonaws.com (54.227.42.64): icmp_req=4 ttl=45 time=97.7 ms
64 bytes from ec2-54-227-42-64.compute-1.amazonaws.com (54.227.42.64): icmp_req=5 ttl=45 time=97.9 ms
64 bytes from ec2-54-227-42-64.compute-1.amazonaws.com (54.227.42.64): icmp_req=6 ttl=45 time=98.1 ms
64 bytes from ec2-54-227-42-64.compute-1.amazonaws.com (54.227.42.64): icmp_req=7 ttl=45 time=97.8 ms
64 bytes from ec2-54-227-42-64.compute-1.amazonaws.com (54.227.42.64): icmp_req=8 ttl=45 time=97.6 ms
64 bytes from ec2-54-227-42-64.compute-1.amazonaws.com (54.227.42.64): icmp_req=9 ttl=45 time=97.8 ms
64 bytes from ec2-54-227-42-64.compute-1.amazonaws.com (54.227.42.64): icmp_req=10 ttl=45 time=97.9 ms

--- ec2-54-227-42-64.compute-1.amazonaws.com ping statistics ---
10 packets transmitted, 10 received, 0% packet loss, time 9014ms
rtt min/avg/max/mdev = 97.627/97.918/98.629/0.366 ms
PING ec2-54-219-6-181.us-west-1.compute.amazonaws.com (54.219.6.181) 56(84) bytes of data.
64 bytes from ec2-54-219-6-181.us-west-1.compute.amazonaws.com (54.219.6.181): icmp_req=1 ttl=42 time=157 ms
64 bytes from ec2-54-219-6-181.us-west-1.compute.amazonaws.com (54.219.6.181): icmp_req=2 ttl=42 time=157 ms
64 bytes from ec2-54-219-6-181.us-west-1.compute.amazonaws.com (54.219.6.181): icmp_req=3 ttl=42 time=156 ms
64 bytes from ec2-54-219-6-181.us-west-1.compute.amazonaws.com (54.219.6.181): icmp_req=4 ttl=42 time=161 ms
64 bytes from ec2-54-219-6-181.us-west-1.compute.amazonaws.com (54.219.6.181): icmp_req=5 ttl=42 time=157 ms
64 bytes from ec2-54-219-6-181.us-west-1.compute.amazonaws.com (54.219.6.181): icmp_req=6 ttl=42 time=157 ms
64 bytes from ec2-54-219-6-181.us-west-1.compute.amazonaws.com (54.219.6.181): icmp_req=7 ttl=42 time=157 ms
64 bytes from ec2-54-219-6-181.us-west-1.compute.amazonaws.com (54.219.6.181): icmp_req=8 ttl=42 time=157 ms
64 bytes from ec2-54-219-6-181.us-west-1.compute.amazonaws.com (54.219.6.181): icmp_req=9 ttl=42 time=184 ms
64 bytes from ec2-54-219-6-181.us-west-1.compute.amazonaws.com (54.219.6.181): icmp_req=10 ttl=42 time=157 ms

--- ec2-54-219-6-181.us-west-1.compute.amazonaws.com ping statistics ---
10 packets transmitted, 10 received, 0% packet loss, time 9011ms
rtt min/avg/max/mdev = 156.836/160.345/184.983/8.306 ms
PING ec2-54-229-197-90.eu-west-1.compute.amazonaws.com (172.31.31.216) 56(84) bytes of data.
64 bytes from ip-172-31-31-216.eu-west-1.compute.internal (172.31.31.216): icmp_req=1 ttl=64 time=0.031 ms
64 bytes from ip-172-31-31-216.eu-west-1.compute.internal (172.31.31.216): icmp_req=2 ttl=64 time=0.031 ms
64 bytes from ip-172-31-31-216.eu-west-1.compute.internal (172.31.31.216): icmp_req=3 ttl=64 time=0.029 ms
64 bytes from ip-172-31-31-216.eu-west-1.compute.internal (172.31.31.216): icmp_req=4 ttl=64 time=0.033 ms
64 bytes from ip-172-31-31-216.eu-west-1.compute.internal (172.31.31.216): icmp_req=5 ttl=64 time=0.026 ms
64 bytes from ip-172-31-31-216.eu-west-1.compute.internal (172.31.31.216): icmp_req=6 ttl=64 time=0.027 ms
64 bytes from ip-172-31-31-216.eu-west-1.compute.internal (172.31.31.216): icmp_req=7 ttl=64 time=0.055 ms
64 bytes from ip-172-31-31-216.eu-west-1.compute.internal (172.31.31.216): icmp_req=8 ttl=64 time=0.055 ms
64 bytes from ip-172-31-31-216.eu-west-1.compute.internal (172.31.31.216): icmp_req=9 ttl=64 time=0.056 ms
64 bytes from ip-172-31-31-216.eu-west-1.compute.internal (172.31.31.216): icmp_req=10 ttl=64 time=0.052 ms

--- ec2-54-229-197-90.eu-west-1.compute.amazonaws.com ping statistics ---
10 packets transmitted, 10 received, 0% packet loss, time 8999ms
rtt min/avg/max/mdev = 0.026/0.039/0.056/0.013 ms
PING ec2-54-238-139-0.ap-northeast-1.compute.amazonaws.com (54.238.139.0) 56(84) bytes of data.
64 bytes from ec2-54-238-139-0.ap-northeast-1.compute.amazonaws.com (54.238.139.0): icmp_req=1 ttl=45 time=272 ms
64 bytes from ec2-54-238-139-0.ap-northeast-1.compute.amazonaws.com (54.238.139.0): icmp_req=2 ttl=46 time=269 ms
64 bytes from ec2-54-238-139-0.ap-northeast-1.compute.amazonaws.com (54.238.139.0): icmp_req=3 ttl=45 time=264 ms
64 bytes from ec2-54-238-139-0.ap-northeast-1.compute.amazonaws.com (54.238.139.0): icmp_req=4 ttl=46 time=271 ms
64 bytes from ec2-54-238-139-0.ap-northeast-1.compute.amazonaws.com (54.238.139.0): icmp_req=5 ttl=45 time=273 ms
64 bytes from ec2-54-238-139-0.ap-northeast-1.compute.amazonaws.com (54.238.139.0): icmp_req=6 ttl=45 time=273 ms
64 bytes from ec2-54-238-139-0.ap-northeast-1.compute.amazonaws.com (54.238.139.0): icmp_req=7 ttl=45 time=272 ms
64 bytes from ec2-54-238-139-0.ap-northeast-1.compute.amazonaws.com (54.238.139.0): icmp_req=8 ttl=45 time=264 ms
64 bytes from ec2-54-238-139-0.ap-northeast-1.compute.amazonaws.com (54.238.139.0): icmp_req=9 ttl=45 time=278 ms
64 bytes from ec2-54-238-139-0.ap-northeast-1.compute.amazonaws.com (54.238.139.0): icmp_req=10 ttl=45 time=264 ms

--- ec2-54-238-139-0.ap-northeast-1.compute.amazonaws.com ping statistics ---
10 packets transmitted, 10 received, 0% packet loss, time 9002ms
rtt min/avg/max/mdev = 264.239/270.528/278.782/4.560 ms
