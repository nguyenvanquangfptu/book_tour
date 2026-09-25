data "aws_ami" "ubuntu" {
  most_recent = true
  owners      = ["099720109477"] # Canonical

  filter {
    name   = "name"
    values = ["ubuntu/images/hvm-ssd/ubuntu-jammy-22.04-amd64-server-*"]
  }
  filter {
    name   = "virtualization-type"
    values = ["hvm"]
  }
}

resource "aws_instance" "backend" {
  ami                         = data.aws_ami.ubuntu.id
  instance_type               = var.instance_type
  subnet_id                   = aws_subnet.public.id
  vpc_security_group_ids      = [aws_security_group.ec2.id]
  iam_instance_profile        = aws_iam_instance_profile.ec2_ssm.name
  associate_public_ip_address = true

  root_block_device {
    volume_type = "gp3"
    volume_size = 20 # duoi han 30GB Free Tier cho toan tai khoan
  }

  user_data = file("${path.module}/user_data.sh")

  tags = { Name = "booktour-backend" }
}

# Elastic IP: chi mien phi khi con gan vao instance dang chay. Neu Stop instance de tiet
# kiem gio Free Tier, PHAI Release hoac giu nguyen gan EIP nay - dung de no roi tu do.
resource "aws_eip" "backend" {
  instance = aws_instance.backend.id
  domain   = "vpc"

  tags = { Name = "booktour-eip" }
}
