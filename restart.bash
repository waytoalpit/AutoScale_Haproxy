iptables -I INPUT -p tcp --dport 80 --syn -j DROP
sleep 1
service haproxy restart
iptables -D INPUT -p tcp --dport 80 --syn -j DROP
