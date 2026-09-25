#!/bin/bash
set -euxo pipefail

# --- Docker + Docker Compose plugin (repo chinh thuc cua Docker cho Ubuntu) ---
apt-get update -y
apt-get install -y ca-certificates curl gnupg
install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc
chmod a+r /etc/apt/keyrings/docker.asc
echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/ubuntu $(. /etc/os-release && echo "$VERSION_CODENAME") stable" \
  > /etc/apt/sources.list.d/docker.list
apt-get update -y
apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

usermod -aG docker ubuntu

# --- SSM Agent (de quan tri qua Session Manager, khong can mo SSH) ---
# Ubuntu khong co san SSM Agent nhu Amazon Linux, phai cai qua snap.
if ! snap list amazon-ssm-agent >/dev/null 2>&1; then
  snap install amazon-ssm-agent --classic
fi
systemctl enable snap.amazon-ssm-agent.amazon-ssm-agent.service || true
systemctl start snap.amazon-ssm-agent.amazon-ssm-agent.service || true

# --- Swap 2GB: t3.micro chi co 1GB RAM, Postgres+Redis+JVM de bi OOM-kill neu khong co swap ---
if [ ! -f /swapfile ]; then
  fallocate -l 2G /swapfile
  chmod 600 /swapfile
  mkswap /swapfile
  swapon /swapfile
  echo '/swapfile none swap sw 0 0' >> /etc/fstab
fi

# --- Thu muc chua .env production va la noi GitLab Runner (cai thu cong sau) se checkout code ---
mkdir -p /opt/booking-tour
chown ubuntu:ubuntu /opt/booking-tour
