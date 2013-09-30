PING ec2-54-227-42-64.compute-1.amazonaws.com (10.117.94.153) 56(84) bytes of data.
64 bytes from ip-10-117-94-153.ec2.internal (10.117.94.153): icmp_req=1 ttl=64 time=0.025 ms
64 bytes from ip-10-117-94-153.ec2.internal (10.117.94.153): icmp_req=2 ttl=64 time=0.054 ms
64 bytes from ip-10-117-94-153.ec2.internal (10.117.94.153): icmp_req=3 ttl=64 time=0.050 ms
64 bytes from ip-10-117-94-153.ec2.internal (10.117.94.153): icmp_req=4 ttl=64 time=0.049 ms
64 bytes from ip-10-117-94-153.ec2.internal (10.117.94.153): icmp_req=5 ttl=64 time=0.054 ms
64 bytes from ip-10-117-94-153.ec2.internal (10.117.94.153): icmp_req=6 ttl=64 time=0.053 ms
64 bytes from ip-10-117-94-153.ec2.internal (10.117.94.153): icmp_req=7 ttl=64 time=0.050 ms
64 bytes from ip-10-117-94-153.ec2.internal (10.117.94.153): icmp_req=8 ttl=64 time=0.045 ms
64 bytes from ip-10-117-94-153.ec2.internal (10.117.94.153): icmp_req=9 ttl=64 time=0.053 ms
64 bytes from ip-10-117-94-153.ec2.internal (10.117.94.153): icmp_req=10 ttl=64 time=0.055 ms

--- ec2-54-227-42-64.compute-1.amazonaws.com ping statistics ---
10 packets transmitted, 10 received, 0% packet loss, time 9004ms
rtt min/avg/max/mdev = 0.025/0.048/0.055/0.012 ms
PING ec2-54-219-6-181.us-west-1.compute.amazonaws.com (54.219.6.181) 56(84) bytes of data.
64 bytes from ec2-54-219-6-181.us-west-1.compute.amazonaws.com (54.219.6.181): icmp_req=1 ttl=45 time=83.8 ms
64 bytes from ec2-54-219-6-181.us-west-1.compute.amazonaws.com (54.219.6.181): icmp_req=2 ttl=45 time=83.8 ms
64 bytes from ec2-54-219-6-181.us-west-1.compute.amazonaws.com (54.219.6.181): icmp_req=3 ttl=45 time=103 ms
64 bytes from ec2-54-219-6-181.us-west-1.compute.amazonaws.com (54.219.6.181): icmp_req=4 ttl=45 time=83.7 ms
64 bytes from ec2-54-219-6-181.us-west-1.compute.amazonaws.com (54.219.6.181): icmp_req=5 ttl=45 time=101 ms
64 bytes from ec2-54-219-6-181.us-west-1.compute.amazonaws.com (54.219.6.181): icmp_req=6 ttl=45 time=83.9 ms
64 bytes from ec2-54-219-6-181.us-west-1.compute.amazonaws.com (54.219.6.181): icmp_req=7 ttl=45 time=84.0 ms
64 bytes from ec2-54-219-6-181.us-west-1.compute.amazonaws.com (54.219.6.181): icmp_req=8 ttl=45 time=122 ms
64 bytes from ec2-54-219-6-181.us-west-1.compute.amazonaws.com (54.219.6.181): icmp_req=9 ttl=45 time=93.9 ms
64 bytes from ec2-54-219-6-181.us-west-1.compute.amazonaws.com (54.219.6.181): icmp_req=10 ttl=45 time=84.0 ms

--- ec2-54-219-6-181.us-west-1.compute.amazonaws.com ping statistics ---
10 packets transmitted, 10 received, 0% packet loss, time 9086ms
rtt min/avg/max/mdev = 83.727/92.502/122.558/12.443 ms
PING ec2-54-229-197-90.eu-west-1.compute.amazonaws.com (54.229.197.90) 56(84) bytes of data.
64 bytes from ec2-54-229-197-90.eu-west-1.compute.amazonaws.com (54.229.197.90): icmp_req=1 ttl=47 time=95.1 ms
64 bytes from ec2-54-229-197-90.eu-west-1.compute.amazonaws.com (54.229.197.90): icmp_req=2 ttl=47 time=94.8 ms
64 bytes from ec2-54-229-197-90.eu-west-1.compute.amazonaws.com (54.229.197.90): icmp_req=3 ttl=47 time=94.9 ms
64 bytes from ec2-54-229-197-90.eu-west-1.compute.amazonaws.com (54.229.197.90): icmp_req=4 ttl=47 time=94.7 ms
64 bytes from ec2-54-229-197-90.eu-west-1.compute.amazonaws.com (54.229.197.90): icmp_req=5 ttl=47 time=94.7 ms
64 bytes from ec2-54-229-197-90.eu-west-1.compute.amazonaws.com (54.229.197.90): icmp_req=6 ttl=47 time=95.1 ms
64 bytes from ec2-54-229-197-90.eu-west-1.compute.amazonaws.com (54.229.197.90): icmp_req=7 ttl=47 time=95.0 ms
64 bytes from ec2-54-229-197-90.eu-west-1.compute.amazonaws.com (54.229.197.90): icmp_req=8 ttl=47 time=95.2 ms
64 bytes from ec2-54-229-197-90.eu-west-1.compute.amazonaws.com (54.229.197.90): icmp_req=9 ttl=47 time=95.5 ms
64 bytes from ec2-54-229-197-90.eu-west-1.compute.amazonaws.com (54.229.197.90): icmp_req=10 ttl=47 time=95.2 ms

--- ec2-54-229-197-90.eu-west-1.compute.amazonaws.com ping statistics ---
10 packets transmitted, 10 received, 0% packet loss, time 9014ms
rtt min/avg/max/mdev = 94.716/95.063/95.586/0.462 ms
PING ec2-54-238-139-0.ap-northeast-1.compute.amazonaws.com (54.238.139.0) 56(84) bytes of data.
64 bytes from ec2-54-238-139-0.ap-northeast-1.compute.amazonaws.com (54.238.139.0): icmp_req=1 ttl=46 time=177 ms
64 bytes from ec2-54-238-139-0.ap-northeast-1.compute.amazonaws.com (54.238.139.0): icmp_req=2 ttl=46 time=184 ms
64 bytes from ec2-54-238-139-0.ap-northeast-1.compute.amazonaws.com (54.238.139.0): icmp_req=3 ttl=46 time=208 ms
64 bytes from ec2-54-238-139-0.ap-northeast-1.compute.amazonaws.com (54.238.139.0): icmp_req=4 ttl=46 time=201 ms
64 bytes from ec2-54-238-139-0.ap-northeast-1.compute.amazonaws.com (54.238.139.0): icmp_req=5 ttl=46 time=188 ms
64 bytes from ec2-54-238-139-0.ap-northeast-1.compute.amazonaws.com (54.238.139.0): icmp_req=6 ttl=46 time=172 ms
64 bytes from ec2-54-238-139-0.ap-northeast-1.compute.amazonaws.com (54.238.139.0): icmp_req=7 ttl=46 time=172 ms
64 bytes from ec2-54-238-139-0.ap-northeast-1.compute.amazonaws.com (54.238.139.0): icmp_req=8 ttl=46 time=173 ms
64 bytes from ec2-54-238-139-0.ap-northeast-1.compute.amazonaws.com (54.238.139.0): icmp_req=9 ttl=46 time=205 ms
64 bytes from ec2-54-238-139-0.ap-northeast-1.compute.amazonaws.com (54.238.139.0): icmp_req=10 ttl=46 time=170 ms

--- ec2-54-238-139-0.ap-northeast-1.compute.amazonaws.com ping statistics ---
10 packets transmitted, 10 received, 0% packet loss, time 9004ms
rtt min/avg/max/mdev = 170.290/185.464/208.884/14.151 ms
