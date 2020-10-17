#!/bin/bash

## CSI Course Project #1 - Linux\Service Hardening.
## Script made by Robert Tiger.
## ------------------------------------------------

## Start of the Project:
figlet "Project #1 - SSH Hardening by Robert Tiger"
sleep 2
echo "This script will harden your SSH service on your machine.
USE WITH CAUTION! it's very easy to get locked out of the service if you don't know what you are doing.
Use ONLY with root user! The script doesn't support sudo commands.
If you are not a root user the script will exit on its own." | pv -qL $[25+(-1 + RANDOM%5)]

while true; do
    read -p "To begin answer yes, to exit answer no: " CONTINUE
    case $CONTINUE in
        [Yy]* ) break;;
        [Nn]* ) exit;;
        * ) echo "Please answer Y/yes or N/no only.";;
    esac
done

USER=$(whoami)
if [ $USER != root ]
then
	echo "Not a root user detected. Can't run the script.
Please log in with user 'root' and try again.
Exiting..."
exit
fi

## Basic info:
#sshd_config file path:
CONFIGLOCATION=$(find "/" -name "sshd_config" | grep -i etc)
LOGFILE=SSH_Hardening_Script.log
LOGLOCATION=/etc/ssh/

## Start of the Script: ##

echo"Restarting SSH service..."
service ssh restart
echo "$(date -u): [RESTART] SSH Service." >> $LOGLOCATION$LOGFILE
sleep 2
echo "Installing necessary program (pv)..."
apt-get install pv -y
echo "pv installed."
echo "$(date -u): [INSTALL] pv." >> $LOGLOCATION$LOGFILE
sleep 2

## Step 1: Creating log file:
touch $LOGLOCATION$LOGFILE.log
echo "Log file created in the name of ${LOGFILE} under ${LOGLOCATION}. View Changes made by reading this file."
echo "$(date -u): [CREATE] This log file." >> $LOGLOCATION$LOGFILE
echo "$(date -u): [START] SSH Hardening Script." >> $LOGLOCATION$LOGFILE
sleep 2

## Step 2: Backup of the sshd_config file:
echo "Create a backup for the sshd_config file." | pv -qL $[20+(-1 + RANDOM%5)]
read -p "Choose a name for the backup file: " BACKUPNAME
echo "Backing up sshd_config file..."
cp $CONFIGLOCATION /etc/ssh/$BACKUPNAME.backup
sleep 2
echo "Backup of sshd_config with a name of ${BACKUPNAME}.backup created and saved under /etc/ssh/."
echo "$(date -u): [CREATE] Backup of sshd_config under /etc/ssh/${BACKUPNAME}" >> $LOGLOCATION$LOGFILE
sleep 2

## Step 3: Changing the port SSH runs on.
read -p "Specify an alternate port you wish SSH to run on (Do NOT use the default port 22!): " PORTNUMBER
sed -i "s/#Port 22/Port $PORTNUMBER/g" $CONFIGLOCATION
sleep 2
echo "SSH port changed to: ${PORTNUMBER}."
echo "$(date -u): [CHANGE] SSH port to: ${PORTNUMBER}." >> $LOGLOCATION$LOGFILE
sleep 2

## Step 4: Configuring the Protocol:
echo "Setting protocol to 2..."
sleep 2
echo -e "Protocol 2 \n" >> $CONFIGLOCATION
echo "$(date -u): [SET] Protocol = 2!" >> $LOGLOCATION$LOGFILE
echo "Protocol set to 2!"
sleep 2

## Step 5: Disabling login to root:
while true; do
    read -p "Would you like to disable login to root via SSH? (Yes\No): " ANSWER
    case $ANSWER in
        [Yy]* ) echo "Disabling login to root via SSH..."
				sed -i "s/#PermitRootLogin prohibit-password/PermitRootLogin no/g" $CONFIGLOCATION
				sleep 2
				echo "Login to root via SSH is now disabled!"
				echo "$(date -u): [CHANGE] '#PermitRootLogin prohibit-password' to 'PermitRootLogin no'." >> $LOGLOCATION$LOGFILE
				sleep 2
				break;;
        [Nn]* ) echo "Moving on..."
				echo "$(date -u): [SKIP] Disabling login to root via SSH." >> $LOGLOCATION$LOGFILE
				sleep 2
				break;;
        * ) 	echo "Please answer Y/yes or N/no only."
				sleep 2
    esac
done

## Step 6: Dissconnect Idle Sessions:
read -p "Set the desired amount of time (In seconds) for a session to be alive. (Recommended: 300): " ALIVE
sed -i "s/#ClientAliveInterval 0/ClientAliveInterval $ALIVE/g" $CONFIGLOCATION
sleep 1
echo "'ClientAliveInterval' set to ${ALIVE} seconds."
sleep 1
read -p "Set the desired number of times to check idle sessions before disconnecting. (Recommended: 2): " COUNT
sed -i "s/#ClientAliveCountMax 3/ClientAliveCountMax $COUNT/g" $CONFIGLOCATION
sleep 1
echo "'ClientAliveCountMax' set to ${COUNT} times."
echo "$(date -u): [CHANGE] 'ClientAliveInterval' to ${ALIVE} seconds." >> $LOGLOCATION$LOGFILE
echo "$(date -u): [CHANGE] 'ClientAliveCountMax' to ${COUNT} times." >> $LOGLOCATION$LOGFILE
sleep 2

## Step 7: Whitelist Users:
echo "This step will grant permission to specific users of your choice to access the SSH service.
This is a white-list! Any other users that are not included in the list will be denied login to SSH.
Please specify the users that are permitted to login." | pv -qL $[20+(-1 + RANDOM%5)]
sleep 1
while :
do
	read -p "Insert 1 user-name at a time (Enter 0 to end): " USER
	if [ ${USER} == 0 ]
		then
		break
	fi
	echo -e "AllowUsers ${USER} \n" >> $CONFIGLOCATION
	sleep 1
done
echo "Users were added to the white-list. you can view and edit them at ${CONFIGLOCATION}."
echo "$(date -u): [ADD] Permitted user(s) to the AllowUsers list at ${CONFIGLOCATION}." >> $LOGLOCATION$LOGFILE
sleep 3

## Step 7: Disable X11Forwarding:
echo "Disabling X11Forwarding..."
sleep 1
sed -i "s/X11Forwarding yes/X11Forwarding no/g" $CONFIGLOCATION
echo "X11Forwarding Disabled..."
echo "$(date -u): [DISABLE] X11Forwarding." >> $LOGLOCATION$LOGFILE
sleep 3

## Step 8: Installing and Configuring fail2ban:
echo "fail2ban service installation starting..."
apt-get install fail2ban -y
echo "fail2ban service installed!"
echo "$(date -u): [INSTALL] fail2ban." >> $LOGLOCATION$LOGFILE
cp /etc/fail2ban/jail.conf /etc/fail2ban/jail.local
echo "$(date -u): [COPY] /etc/fail2ban/jail.conf as jail.local in the same directory." >> $LOGLOCATION$LOGFILE
echo "Configuring jail.local file under /etc/fail2ban/..."
sleep 3
sed -i '0,/# \[sshd\]/{s/# \[sshd\]/[sshd]/}' /etc/fail2ban/jail.local
echo "$(date -u): [UNCOMMENT] first occurrence of pattern '[sshd]'." >> $LOGLOCATION$LOGFILE
sed -i '0,/# enabled = true/{s/# enabled = true/enabled = true/}' /etc/fail2ban/jail.local
echo "$(date -u): [UNCOMMENT] first occurrence of pattern 'enabled = true'." >> $LOGLOCATION$LOGFILE
sed -i '24a\port = ssh' /etc/fail2ban/jail.local
echo "$(date): [ADD] 'port = ssh' to etc/fail2ban/jail.local in line 25." >> $LOGLOCATION$LOGFILE
sed -i '25a\logpath = %(sshd_log)s' /etc/fail2ban/jail.local
echo "$(date): [ADD] 'logpath = %(sshd_log)s' to etc/fail2ban/jail.local in line 26." >> $LOGLOCATION$LOGFILE
echo "Configuration completed, Restarting fail2ban service..."
service fail2ban restart
echo "$(date -u): [RESTART] fail2ban." >> $LOGLOCATION$LOGFILE
sleep 2

## Step 9: SSH Audit:
echo "Restarting SSH service..." | pv -qL $[20+(-1 + RANDOM%5)]
echo "$(date -u): [RESTART] SSH Service." >> $LOGLOCATION$LOGFILE
echo "Getting ready to install ssh-audit." | pv -qL $[20+(-1 + RANDOM%5)]
read -p "Choose the directory where ssh-audit will be installed on your machine.
Use the following example: Desktop/SomePath/SomeDirectory >> " SSHAUDITPATH
cd ~/$SSHAUDITPATH
git clone https://github.com/arthepsy/ssh-audit.git
echo "$(date -u): [INSTALL] SSH Audit." >> $LOGLOCATION$LOGFILE
IP=$(ifconfig | grep broadcast | awk '{print$2}')
cd ssh-audit/ && ./ssh-audit.py $IP:$PORTNUMBER
sleep 1
echo "Changing HostKey at ${CONFIGLOCATION}."
echo -e "HostKey /etc/ssh/ssh_host_ed25519_key
HostKey /etc/ssh/ssh_host_rsa_key \n" >> $CONFIGLOCATION
echo "$(date -u): [CHANGE] HostKey configuration at ${CONFIGLOCATION}." >> $LOGLOCATION$LOGFILE
sleep 1
echo "Changing KexAlgorithms at ${CONFIGLOCATION}."
echo "KexAlgorithms curve25519-sha256@libssh.org
Ciphers chacha20-poly1305@openssh.com,aes256-gcm@openssh.com,aes128-gcm@openssh.com,aes256-ctr,aes192-ctr,aes128-ctr
MACs hmac-sha2-512-etm@openssh.com,hmac-sha2-256-etm@openssh.com,umac-128-etm@openssh.com" >> $CONFIGLOCATION
echo "$(date -u): [CHANGE] KexAlgroithms configuration at ${CONFIGLOCATION}."  >> $LOGLOCATION$LOGFILE
sleep 1
echo "Restarting the SSH service..." | pv -qL $[17+(-1 + RANDOM%5)]
service ssh restart
echo "$(date -u): [RESTART] SSH Service." >> $LOGLOCATION$LOGFILE
sleep 2
echo "Results:"
./ssh-audit.py $IP:$PORTNUMBER

## Ending Comments:
echo "Thanks for using the script. Your SSH service will now be a bit more hard to break into.
Exiting..." | pv -qL $[20+(-1 + RANDOM%5)]
sleep 2
