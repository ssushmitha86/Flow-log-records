# Flow-log-records
This project contains 3 input files in `data/` folder  
* **protocol.csv** has mappings of (protocol number,protocol name)  
* **tag_mappings.csv** has mappings of (destination port,protocol name,tag)  
* **log.txt** has version 2 logs provided in the question

### Assumption
* Both the protocol.csv and tag_mappings.csv has valid data with 2 and 3 comma separated entries in each line.Invalid lines are skipped.
* Log file has space separated columns
* To support any custom version of log data, destination port column and protocol number column is recieved as input from the user.Lines which doesn't have sufficient number of columns are skipped
  
To run this project **JDK** and **Maven** are required  
  
Output files are generated in the `data/` folder  

Zip contains demo of the working code
[Log_Processing.zip](https://github.com/user-attachments/files/17125779/Log_Processing.zip)
