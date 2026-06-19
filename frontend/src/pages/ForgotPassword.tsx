import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { FaEnvelope, FaLock, FaKey, FaArrowLeft } from 'react-icons/fa';
import { AuthService } from '../services/AuthService';
import '../styles/auth.css';

const ForgotPassword: React.FC = () => {
  const navigate = useNavigate();
  
  const [step, setStep] = useState<1 | 2>(1);
  const [email, setEmail] = useState('');
  const [token, setToken] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(false);
  const [countdown, setCountdown] = useState(0);

  useEffect(() => {
    let timer: any;
    if (countdown > 0) {
      timer = setTimeout(() => setCountdown(countdown - 1), 1000);
    }
    return () => clearTimeout(timer);
  }, [countdown]);

  const handleRequestToken = async (e?: React.FormEvent) => {
    if (e) e.preventDefault();
    if (!email) {
      setError('Vui lòng nhập email');
      return;
    }
    
    setLoading(true);
    setError('');
    setSuccess('');
    
    try {
      await AuthService.forgotPassword(email);
      setSuccess('Mã xác thực đã được gửi đến email của bạn.');
      setStep(2);
      setCountdown(60);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Không thể gửi mã xác thực. Vui lòng kiểm tra lại email.');
      console.error('Forgot password error', err);
    } finally {
      setLoading(false);
    }
  };

  const handleResetPassword = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (newPassword.length < 8) {
      setError('Mật khẩu mới phải có ít nhất 8 ký tự.');
      return;
    }
    
    if (newPassword !== confirmPassword) {
      setError('Mật khẩu xác nhận không khớp.');
      return;
    }
    
    setLoading(true);
    setError('');
    setSuccess('');
    
    try {
      await AuthService.resetPassword(token, newPassword);
      setSuccess('Đổi mật khẩu thành công! Bạn có thể đăng nhập bằng mật khẩu mới.');
      // Auto redirect after 3s
      setTimeout(() => navigate('/login'), 3000);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Đổi mật khẩu thất bại. Mã xác thực không đúng hoặc đã hết hạn.');
      console.error('Reset password error', err);
    } finally {
      setLoading(false);
    }
  };

  const isPasswordShort = newPassword.length > 0 && newPassword.length < 8;
  const isPasswordMismatch = confirmPassword.length > 0 && newPassword !== confirmPassword;
  const isFormInvalid = isPasswordShort || isPasswordMismatch;

  return (
    <div className="auth-page">
      <div className="auth-overlay"></div>
      <div className="auth-container animate-fade-in">
        <div className="auth-card glass-card">
          <div className="auth-header">
            <h2>Khôi phục mật khẩu</h2>
            <p>{step === 1 ? 'Nhập email để nhận mã xác thực' : 'Nhập mã xác thực và mật khẩu mới'}</p>
          </div>

          {error && <div className="auth-error">{error}</div>}
          {success && <div className="auth-success" style={{ color: '#2ecc71', backgroundColor: 'rgba(46, 204, 113, 0.1)', padding: '10px', borderRadius: '5px', marginBottom: '15px', fontSize: '14px', border: '1px solid #2ecc71' }}>{success}</div>}

          {step === 1 ? (
            <form onSubmit={handleRequestToken} className="auth-form" autoComplete="off">
              <div className="input-group">
                <label className="input-label">Email của bạn</label>
                <div className="auth-input-wrapper">
                  <FaEnvelope className="auth-icon" />
                  <input 
                    type="email" 
                    className="input-field" 
                    placeholder="Nhập địa chỉ email"
                    value={email}
                    autoComplete="email"
                    onChange={(e) => setEmail(e.target.value)}
                    required 
                  />
                </div>
              </div>

              <button type="submit" className="btn btn-primary auth-btn" disabled={loading}>
                {loading ? 'Đang gửi...' : 'Gửi mã xác thực'}
              </button>
            </form>
          ) : (
            <form onSubmit={handleResetPassword} className="auth-form" autoComplete="off">
              <div className="input-group">
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '5px' }}>
                  <label className="input-label" style={{ marginBottom: 0 }}>Mã xác thực (Token)</label>
                  <button 
                    type="button" 
                    onClick={(e) => handleRequestToken(e)} 
                    disabled={countdown > 0 || loading}
                    style={{ 
                      background: 'none', 
                      border: 'none', 
                      color: countdown > 0 ? '#95a5a6' : '#3498db', 
                      cursor: countdown > 0 ? 'not-allowed' : 'pointer',
                      fontSize: '13px',
                      fontWeight: '500',
                      padding: 0
                    }}
                  >
                    {countdown > 0 ? `Gửi lại sau ${countdown}s` : 'Gửi lại mã'}
                  </button>
                </div>
                <div className="auth-input-wrapper">
                  <FaKey className="auth-icon" />
                  <input 
                    type="text" 
                    className="input-field" 
                    placeholder="Nhập mã OTP từ email"
                    value={token}
                    onChange={(e) => setToken(e.target.value)}
                    autoComplete="one-time-code"
                    required 
                  />
                </div>
              </div>

              <div className="input-group">
                <label className="input-label">Mật khẩu mới</label>
                <div className="auth-input-wrapper">
                  <FaLock className="auth-icon" />
                  <input 
                    type="password" 
                    className="input-field" 
                    placeholder="Nhập mật khẩu mới"
                    value={newPassword}
                    onChange={(e) => setNewPassword(e.target.value)}
                    autoComplete="new-password"
                    required 
                  />
                </div>
                {isPasswordShort && <span style={{color: '#e74c3c', fontSize: '13px', marginTop: '4px', display: 'block', textAlign: 'left', fontWeight: '500'}}>Mật khẩu phải có ít nhất 8 ký tự.</span>}
              </div>
              
              <div className="input-group">
                <label className="input-label">Xác nhận mật khẩu mới</label>
                <div className="auth-input-wrapper">
                  <FaLock className="auth-icon" />
                  <input 
                    type="password" 
                    className="input-field" 
                    placeholder="Xác nhận mật khẩu"
                    value={confirmPassword}
                    onChange={(e) => setConfirmPassword(e.target.value)}
                    autoComplete="new-password"
                    required 
                  />
                </div>
                {isPasswordMismatch && <span style={{color: '#e74c3c', fontSize: '13px', marginTop: '4px', display: 'block', textAlign: 'left', fontWeight: '500'}}>Mật khẩu xác nhận không khớp.</span>}
              </div>

              <button type="submit" className="btn btn-primary auth-btn" disabled={loading || isFormInvalid}>
                {loading ? 'Đang xử lý...' : 'Xác nhận đổi mật khẩu'}
              </button>
            </form>
          )}

          <div className="auth-footer" style={{ marginTop: '24px' }}>
            <Link to="/login" className="auth-link" style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '5px' }}>
              <FaArrowLeft /> Quay lại đăng nhập
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ForgotPassword;
