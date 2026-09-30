# -- 1) assume that aws ecs was already launched
# -- 2) connect to the ec2 instance from local terminal (assum that is also not needed if using terminal in aws window)
# Assuming the key name is AWS_EC2_DemoKey.pem available in the pwd
chmod 400 AWS_EC2_DemoKey.pem
# Assuming the public DNS is: ec2-18-221-37-196.us-east-2.compute.amazonaws.com
ssh -i "AWS_EC2_DemoKey.pem" ec2-user@ec2-18-221-37-196.us-east-2.compute.amazonaws.com

# -- 3) install docker
# update the existing packages
sudo yum update
# download and install Docker
sudo yum install docker
# Add the $USER user to the "docker" user group 
# The current $USER is ec2-user
sudo usermod -a -G docker $USER
sudo reboot

# -- 4) Create and run your Container
 start Docker service
sudo service docker start
# Check if the Docker engine is running
systemctl show --property ActiveState docker
# Create and run a new Container using the "jenkinsci/blueocean" image
docker run -u root -d --name myContainer -p 8080:8080 -v jenkins-data:/var/jenkins_home -v /var/run/docker.sock:/var/run/docker.sock -v "$HOME":/home jenkinsci/blueocean

# -- 5) Create an RSA key pair inside the container
# Open a shell into myContainer. The container name may vary in your case
docker exec -it myContainer bash
# Since our project is a Maven project, we need to install Maven in the container
apk add maven
# Generate RSA key-pair. It will generate a public and private key. 
# We will place the public key in the Github account, and the private key in the Jenkins console
ssh-keygen -t rsa
# View the private key
cat /root/.ssh/id_rsa
# View the pubic key 
cat /root/.ssh/id_rsa.pub

# -- 6) Admin login to Jenkins console
# Run the following commands in the host EC2 instance's terminal
docker ps
# Use the container ID from the command above
docker logs <conatiner_id>
# Open the bash into the container
docker exec -it myContainer bash
# View the file
cat /var/jenkins_home/secrets/initialAdminPassword

## -- 7) Add private key to Jenkins global credentials
# Open the bash into the container, if you have exited from the bash
docker exec -it myContainer bash
# View the private key
cat /root/.ssh/id_rsa

# -- 8) Add public key to Github repository
# View the pubic key 
cat /root/.ssh/id_rsa.pub