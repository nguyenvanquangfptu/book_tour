import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { FaUser, FaEnvelope, FaLock, FaUserPlus } from 'react-icons/fa';
import { GoogleLogin } from '@react-oauth/google';
import { AuthService } from '../services/AuthService';
import '../styles/auth.css';

const Register: React.FC = () => {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    fullName: '',
    username: '',
    email: '',
    password: '',
    confirmPassword: ''
  });
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(false);
  const [loading, setLoading] = useState(false);

  // States to prevent autofill
  const [focusStates, setFocusStates] = useState({
    fullName: false,
    username: false,
    email: false,
    password: false,
    confirmPassword: false
  });

  const handleFocus = (field: string) => {
    setFocusStates(prev => ({ ...prev, [field]: true }));
  };

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    
    if (formData.password !== formData.confirmPassword) {
      setError('Mật khẩu xác nhận không khớp!');
      setLoading(false);
      return;
    }

    try {
      const registerData = {
        fullName: formData.fullName,
        username: formData.username,
        email: formData.email,
        password: formData.password
      };
      
      const data = await AuthService.register(registerData);
      if (data) {
        setSuccess(true);
        setTimeout(() => {
          navigate('/login');
        }, 2000);
      }
    } catch (err: any) {
      setError(err.response?.data?.message || 'Đăng ký thất bại. Vui lòng thử lại sau.');
      console.error('Register error', err);
    } finally {
      setLoading(false);
    }
  };

  const handleGoogleSuccess = async (credentialResponse: any) => {
    setLoading(true);
    setError('');
    try {
      const { credential } = credentialResponse;
      const data = await AuthService.googleLogin(credential);
      if (data && data.token) {
        localStorage.setItem('token', data.token);
        localStorage.setItem('user', JSON.stringify({
          id: data.userId,
          username: data.username,
          role: data.role
        }));
        navigate('/');
        window.location.reload();
      }
    } catch (err: any) {
      setError(err.response?.data?.message || 'Đăng nhập bằng Google thất bại.');
      console.error('Google login error', err);
    } finally {
      setLoading(false);
    }
  };

  const isPasswordShort = formData.password.length > 0 && formData.password.length < 8;
  const isPasswordMismatch = formData.confirmPassword.length > 0 && formData.password !== formData.confirmPassword;
  const isFormInvalid = isPasswordShort || isPasswordMismatch;

  return (
    <div className="auth-page">
      <div className="auth-overlay"></div>
      <div className="auth-container animate-fade-in">
        <div className="auth-card glass-card">
          <div className="auth-header">
            <h2>Tạo Tài Khoản Mới</h2>
            <p>Tham gia cùng chúng tôi để đặt tour dễ dàng hơn</p>
          </div>

          {error && <div className="auth-error">{error}</div>}
          {success && <div className="auth-success">Đăng ký thành công! Đang chuyển hướng đến trang đăng nhập...</div>}

          <form onSubmit={handleSubmit} className="auth-form" autoComplete="off">
            <div className="input-group">
              <label className="input-label">Họ và tên</label>
              <div className="auth-input-wrapper">
                <FaUser className="auth-icon" />
                <input 
                  type="text" 
                  name="fullName"
                  className="input-field" 
                  placeholder="Nhập họ và tên của bạn"
                  value={formData.fullName}
                  onChange={handleInputChange}
                  autoComplete="name"
                  readOnly={!focusStates.fullName}
                  onFocus={() => handleFocus('fullName')}
                  required 
                />
              </div>
            </div>

            <div className="input-group">
              <label className="input-label">Tên đăng nhập</label>
              <div className="auth-input-wrapper">
                <FaUser className="auth-icon" />
                <input 
                  type="text" 
                  name="username"
                  className="input-field" 
                  placeholder="Nhập tên đăng nhập"
                  value={formData.username}
                  onChange={handleInputChange}
                  autoComplete="username"
                  readOnly={!focusStates.username}
                  onFocus={() => handleFocus('username')}
                  required 
                />
              </div>
            </div>

            <div className="input-group">
              <label className="input-label">Email</label>
              <div className="auth-input-wrapper">
                <FaEnvelope className="auth-icon" />
                <input 
                  type="email" 
                  name="email"
                  className="input-field" 
                  placeholder="example@gmail.com"
                  value={formData.email}
                  onChange={handleInputChange}
                  autoComplete="email"
                  readOnly={!focusStates.email}
                  onFocus={() => handleFocus('email')}
                  required 
                />
              </div>
            </div>

            <div className="input-group">
              <label className="input-label">Mật khẩu</label>
              <div className="auth-input-wrapper">
                <FaLock className="auth-icon" />
                <input 
                  type="password" 
                  name="password"
                  className="input-field" 
                  placeholder="Tạo mật khẩu"
                  value={formData.password}
                  onChange={handleInputChange}
                  autoComplete="new-password"
                  readOnly={!focusStates.password}
                  onFocus={() => handleFocus('password')}
                  required 
                />
              </div>
              {isPasswordShort && <span style={{color: '#e74c3c', fontSize: '13px', marginTop: '4px', display: 'block', textAlign: 'left', fontWeight: '500'}}>Mật khẩu phải có ít nhất 8 ký tự.</span>}
            </div>

            <div className="input-group">
              <label className="input-label">Xác nhận mật khẩu</label>
              <div className="auth-input-wrapper">
                <FaLock className="auth-icon" />
                <input 
                  type="password" 
                  name="confirmPassword"
                  className="input-field" 
                  placeholder="Nhập lại mật khẩu"
                  value={formData.confirmPassword}
                  onChange={handleInputChange}
                  autoComplete="new-password"
                  readOnly={!focusStates.confirmPassword}
                  onFocus={() => handleFocus('confirmPassword')}
                  required 
                />
              </div>
              {isPasswordMismatch && <span style={{color: '#e74c3c', fontSize: '13px', marginTop: '4px', display: 'block', textAlign: 'left', fontWeight: '500'}}>Mật khẩu xác nhận không khớp.</span>}
            </div>

            <button type="submit" className="btn btn-primary auth-btn" disabled={loading || success || isFormInvalid}>
              {loading ? 'Đang xử lý...' : (
                <>
                  <FaUserPlus /> Đăng ký
                </>
              )}
            </button>
          </form>

          <div className="auth-divider">
            <span>Hoặc đăng ký với</span>
          </div>

          <div className="auth-social" style={{ display: 'flex', justifyContent: 'center', marginTop: '16px' }}>
            <GoogleLogin
              onSuccess={handleGoogleSuccess}
              onError={() => {
                setError('Đăng nhập bằng Google thất bại.');
              }}
              useOneTap
            />
          </div>

          <div className="auth-footer" style={{ marginTop: '24px' }}>
            <p>Đã có tài khoản? <Link to="/login" className="auth-link">Đăng nhập</Link></p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Register;
