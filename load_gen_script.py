#### Execution Steps #####
###   python load_gen_script.py <load_balan_ip> <trace_file_name>   ### 
##########################

import os
import sys
import time
import subprocess
# /httperf-0.9.0-varrarive/src/httperf --server=r3r3u13 --uri=/cgi-bin/memcache_m_threaded_sort.php --num-conns=100 --period=60:e0.01000 --period=40:e0.00714

## Reading Inputs from command Line, 
load_bal_ip = str(sys.argv[1])  ## 52.37.198.128
trace_file_name = str(sys.argv[2])  ## trace_temp.txt, this file will define the variation in load.  In profs email he mentioned files as follows:- wc=slowly varying, t2=spike, t4=dual, jagged=quickly varying

lines = open("trace_files/"+trace_file_name).read().split('\n')
numConns=0; listLines =[]
for line in lines:
	# print line;
   	listLines.append(line.strip())
   	numConns = numConns + int(line)
# print numConns
# print listLines


## <<<<<< Begins: New Logic of firing a separate httperf command with only one --period <trace_value>  >>>>>>>
## Note:- the format is --period=100:d<10/100> ie; 100 request spread in span of 10 seconds, so basically every request spanned over 10 seconds
### Now Prepare the period format ###
strPeriod = ""
output_filename = ""
count = 0
for line in listLines:
    count += 1
    strPeriod = " --period=" + line +":d" + str(float(10/float(line)))
    output_filename = "output/output_" + line
    strCommand = "httperf --server "+load_bal_ip+"  --port 80 --num-conns=" + line + " --timeout=0.5" + strPeriod + " > " + output_filename
    print strCommand
    os.system(strCommand)
    print "\n\n\n"
    #strCommand = "grep RT:" + output_filename + " | awk '{ if($4 >= 6) print $4}' | wc -l" + " >> " + "result.txt"
    p1 = subprocess.Popen(["grep", "RT:", output_filename], stdout=subprocess.PIPE)
    p2 = subprocess.Popen(["awk", "{if($4 >= 6) print $4}"], stdin=p1.stdout, stdout=subprocess.PIPE)
    cmd = 'wc -l >> final_result.txt'
    #p3 = subprocess.Popen(["wc", "-l", ">>", "result.txt"], stdin=p2.stdout, stdout=subprocess.PIPE)
    p3 = subprocess.Popen(cmd, shell=True, stdin=p2.stdout, stdout=subprocess.PIPE)
    p1.stdout.close()
    p2.stdout.close()
    #output, err = p2.communicate()
    #output = re.findall(r'\d+', str(output))
    p3.stdout.close()
    #time.sleep(0.02)
    # print strPeriod



# os.system(strCommand)
###### This is the format in example.txt  ### /httperf-0.9.0-varrarive/src/httperf --server=r3r3u13 --uri=/cgi-bin/memcache_m_threaded_sort.php --num-conns=100 --period=60:e0.01000 --period=40:e0.00714

## <<<<<< Ends: New Logic of firing a separate httperf command with only one --period <trace_value>


## <<<<<< Begins: Old Logic of firing a single command of Httperf with multiple --period --period --
'''
### Now Prepare the period format ###
strPeriod = ""
for line in listLines:
    strPeriod = strPeriod + " --period=" + line +":d" + str(float(1/float(line)))
# print strPeriod

strCommand = "httperf --server "+load_bal_ip+"  --port 80 --num-conns=" + str(numConns) + " --timeout=0.5" + strPeriod
print strCommand

os.system(strCommand)
###### This is the format in example.txt  ### /httperf-0.9.0-varrarive/src/httperf --server=r3r3u13 --uri=/cgi-bin/memcache_m_threaded_sort.php --num-conns=100 --period=60:e0.01000 --period=40:e0.00714
'''
## <<<<<< Ends: Old Logic of firing a single command of Httperf with multiple --period --period --


### How to do cd through python script in ubuntu terminal? This way --> #### os.chdir("httperf-0.9.0-varrarive/src")
