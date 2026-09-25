# Cho phep quan tri EC2 qua AWS Systems Manager Session Manager - khong can mo port 22,
# khong phu thuoc IP ca nhan (hay bi doi/mang chan). Xac thuc qua IAM thay vi SSH key.
data "aws_iam_policy_document" "ec2_assume_role" {
  statement {
    actions = ["sts:AssumeRole"]
    principals {
      type        = "Service"
      identifiers = ["ec2.amazonaws.com"]
    }
  }
}

resource "aws_iam_role" "ec2_ssm" {
  name               = "booktour-ec2-ssm-role"
  assume_role_policy = data.aws_iam_policy_document.ec2_assume_role.json

  tags = { Name = "booktour-ec2-ssm-role" }
}

resource "aws_iam_role_policy_attachment" "ec2_ssm_core" {
  role       = aws_iam_role.ec2_ssm.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonSSMManagedInstanceCore"
}

resource "aws_iam_instance_profile" "ec2_ssm" {
  name = "booktour-ec2-ssm-profile"
  role = aws_iam_role.ec2_ssm.name
}
