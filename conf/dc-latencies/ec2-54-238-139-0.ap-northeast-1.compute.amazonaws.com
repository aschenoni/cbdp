PING ec2-54-227-42-64.compute-1.amazonaws.com (54.227.42.64) 56(84) bytes of data.
64 bytes from ec2-54-227-42-64.compute-1.amazonaws.com (54.227.42.64): icmp_req=1 ttl=46 time=200 ms
64 bytes from ec2-54-227-42-64.compute-1.amazonaws.com (54.227.42.64): icmp_req=2 ttl=46 time=177 ms
64 bytes from ec2-54-227-42-64.compute-1.amazonaws.com (54.227.42.64): icmp_req=3 ttl=46 time=201 ms
64 bytes from ec2-54-227-42-64.compute-1.amazonaws.com (54.227.42.64): icmp_req=4 ttl=46 time=203 ms
64 bytes from ec2-54-227-42-64.compute-1.amazonaws.com (54.227.42.64): icmp_req=5 ttl=46 time=174 ms
64 bytes from ec2-54-227-42-64.compute-1.amazonaws.com (54.227.42.64): icmp_req=6 ttl=46 time=184 ms
64 bytes from ec2-54-227-42-64.compute-1.amazonaws.com (54.227.42.64): icmp_req=7 ttl=46 time=174 ms
64 bytes from ec2-54-227-42-64.compute-1.amazonaws.com (54.227.42.64): icmp_req=8 ttl=46 time=184 ms
64 bytes from ec2-54-227-42-64.compute-1.amazonaws.com (54.227.42.64): icmp_req=9 ttl=46 time=169 ms
64 bytes from ec2-54-227-42-64.compute-1.amazonaws.com (54.227.42.64): icmp_req=10 ttl=46 time=172 ms

--- ec2-54-227-42-64.compute-1.amazonaws.com ping statistics ---
10 packets transmitted, 10 received, 0% packet loss, time 9011ms
rtt min/avg/max/mdev = 169.611/184.288/203.605/12.372 ms
PING ec2-54-219-6-181.us-west-1.compute.amazonaws.com (54.219.6.181) 56(84) bytes of data.
64 bytes from ec2-54-219-6-181.us-west-1.compute.amazonaws.com (54.219.6.181): icmp_req=1 ttl=47 time=136 ms
64 bytes from ec2-54-219-6-181.us-west-1.compute.amazonaws.com (54.219.6.181): icmp_req=2 ttl=47 time=168 ms
64 bytes from ec2-54-219-6-181.us-west-1.compute.amazonaws.com (54.219.6.181): icmp_req=3 ttl=47 time=125 ms
64 bytes from ec2-54-219-6-181.us-west-1.compute.amazonaws.com (54.219.6.181): icmp_req=4 ttl=47 time=125 ms
64 bytes from ec2-54-219-6-181.us-west-1.compute.amazonaws.com (54.219.6.181): icmp_req=5 ttl=47 time=127 ms
64 bytes from ec2-54-219-6-181.us-west-1.compute.amazonaws.com (54.219.6.181): icmp_req=6 ttl=47 time=125 ms
64 bytes from ec2-54-219-6-181.us-west-1.compute.amazonaws.com (54.219.6.181): icmp_req=7 ttl=47 time=134 ms
64 bytes from ec2-54-219-6-181.us-west-1.compute.amazonaws.com (54.219.6.181): icmp_req=8 ttl=47 time=136 ms
64 bytes from ec2-54-219-6-181.us-west-1.compute.amazonaws.com (54.219.6.181): icmp_req=9 ttl=47 time=128 ms
64 bytes from ec2-54-219-6-181.us-west-1.compute.amazonaws.com (54.219.6.181): icmp_req=10 ttl=47 time=137 ms

--- ec2-54-219-6-181.us-west-1.compute.amazonaws.com ping statistics ---
10 packets transmitted, 10 received, 0% packet loss, time 9012ms
rtt min/avg/max/mdev = 125.410/134.734/168.897/12.309 ms
PING ec2-54-229-197-90.eu-west-1.compute.amazonaws.com (54.229.197.90) 56(84) bytes of data.
64 bytes from ec2-54-229-197-90.eu-west-1.compute.amazonaws.com (54.229.197.90): icmp_req=1 ttl=45 time=264 ms
64 bytes from ec2-54-229-197-90.eu-west-1.compute.amazonaws.com (54.229.197.90): icmp_req=2 ttl=45 time=267 ms
64 bytes from ec2-54-229-197-90.eu-west-1.compute.amazonaws.com (54.229.197.90): icmp_req=3 ttl=45 time=273 ms
64 bytes from ec2-54-229-197-90.eu-west-1.compute.amazonaws.com (54.229.197.90): icmp_req=4 ttl=45 time=269 ms
64 bytes from ec2-54-229-197-90.eu-west-1.compute.amazonaws.com (54.229.197.90): icmp_req=5 ttl=45 time=264 ms
64 bytes from ec2-54-229-197-90.eu-west-1.compute.amazonaws.com (54.229.197.90): icmp_req=6 ttl=45 time=273 ms
64 bytes from ec2-54-229-197-90.eu-west-1.compute.amazonaws.com (54.229.197.90): icmp_req=7 ttl=45 time=269 ms
64 bytes from ec2-54-229-197-90.eu-west-1.compute.amazonaws.com (54.229.197.90): icmp_req=8 ttl=45 time=264 ms
64 bytes from ec2-54-229-197-90.eu-west-1.compute.amazonaws.com (54.229.197.90): icmp_req=9 ttl=45 time=270 ms
64 bytes from ec2-54-229-197-90.eu-west-1.compute.amazonaws.com (54.229.197.90): icmp_req=10 ttl=45 time=269 ms

--- ec2-54-229-197-90.eu-west-1.compute.amazonaws.com ping statistics ---
10 packets transmitted, 10 received, 0% packet loss, time 9003ms
rtt min/avg/max/mdev = 264.708/268.827/273.783/3.253 ms
PING ec2-54-238-139-0.ap-northeast-1.compute.amazonaws.com (172.31.13.45) 56(84) bytes of data.
64 bytes from ip-172-31-13-45.ap-northeast-1.compute.internal (172.31.13.45): icmp_req=1 ttl=64 time=0.031 ms
64 bytes from ip-172-31-13-45.ap-northeast-1.compute.internal (172.31.13.45): icmp_req=2 ttl=64 time=0.054 ms
64 bytes from ip-172-31-13-45.ap-northeast-1.compute.internal (172.31.13.45): icmp_req=3 ttl=64 time=0.056 ms
64 bytes from ip-172-31-13-45.ap-northeast-1.compute.internal (172.31.13.45): icmp_req=4 ttl=64 time=0.055 ms
64 bytes from ip-172-31-13-45.ap-northeast-1.compute.internal (172.31.13.45): icmp_req=5 ttl=64 time=0.055 ms
64 bytes from ip-172-31-13-45.ap-northeast-1.compute.internal (172.31.13.45): icmp_req=6 ttl=64 time=0.056 ms
64 bytes from ip-172-31-13-45.ap-northeast-1.compute.internal (172.31.13.45): icmp_req=7 ttl=64 time=0.057 ms
64 bytes from ip-172-31-13-45.ap-northeast-1.compute.internal (172.31.13.45): icmp_req=8 ttl=64 time=0.054 ms
64 bytes from ip-172-31-13-45.ap-northeast-1.compute.internal (172.31.13.45): icmp_req=9 ttl=64 time=0.053 ms
64 bytes from ip-172-31-13-45.ap-northeast-1.compute.internal (172.31.13.45): icmp_req=10 ttl=64 time=0.055 ms

--- ec2-54-238-139-0.ap-northeast-1.compute.amazonaws.com ping statistics ---
10 packets transmitted, 10 received, 0% packet loss, time 9008ms
rtt min/avg/max/mdev = 0.031/0.052/0.057/0.010 ms
