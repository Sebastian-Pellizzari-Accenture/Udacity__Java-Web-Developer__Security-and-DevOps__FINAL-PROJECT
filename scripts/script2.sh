#-- 2) Checking on the EEC2 instance
# Note that the Public IP addresses of an EC2 instance keep changing after every reboot
# Replace the key file name and DNS as applicable to you
ssh -i "AWS_EC2_DemoKey.pem" ec2-user@ec2-18-222-193-10.us-east-2.compute.amazonaws.com
# Start Docker service
sudo service docker start
# Check if the Docker engine is running
systemctl show --property ActiveState docker
# Check the stopped containers
docker ps --filter "status=exited"
# Check the running containers
docker ps
# IF JENKINS COTAINER STOPPED
# docker start <conatiner_name/ID>
####    ====================CREATE AND RUN A NEW CONTAINER=======================
# -- 1) Run the container
docker run -dit --name myTomcatServer -p 8888:8080 tomcat:jdk8