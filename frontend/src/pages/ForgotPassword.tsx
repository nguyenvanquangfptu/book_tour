import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { FaEnvelope, FaLock, FaKey, FaArrowLeft } from 'react-icons/fa';
import { AuthService } from '../services/AuthService';
import { useTranslation } from 'react-i18next';
import '../styles/auth.css';

const ForgotPassword: React.FC = () => {
  const { t } = useTranslation();
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
      setError(t('auth.enterEmail'));
      return;
    }
    
    setLoading(true);
    setError('');
    setSuccess('');
    
    try {
      await AuthService.forgotPassword(email);
      setSuccess(t('auth.tokenSent'));
      setStep(2);
      setCountdown(60);
    } catch (err: any) {
      setError(err.response?.data?.message || t('auth.sendTokenFail'));
      console.error('Forgot password error', err);
    } finally {
      setLoading(false);
    }
  };

  const handleResetPassword = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (newPassword.length < 8) {
      setError(t('auth.passwordShort'));
      return;
    }
    
    if (newPassword !== confirmPassword) {
      setError(t('auth.passwordMismatch'));
      return;
    }
    
    setLoading(true);
    setError('');
    setSuccess('');
    
    try {
      await AuthService.resetPassword(token, newPassword);
      setSuccess(t('auth.resetSuccess'));
      // Auto redirect after 3s
      setTimeout(() => navigate('/login'), 3000);
    } catch (err: any) {
      setError(err.response?.data?.message || t('auth.resetFail'));
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
            <h2>{t('auth.forgotTitle')}</h2>
            <p>{step === 1 ? t('auth.forgotSubtitle1') : t('auth.forgotSubtitle2')}</p>
          </div>

          {error && <div className="auth-error">{error}</div>}
          {success && <div className="auth-success" style={{ color: '#2ecc71', backgroundColor: 'rgba(46, 204, 113, 0.1)', padding: '10px', borderRadius: '5px', marginBottom: '15px', fontSize: '14px', border: '1px solid #2ecc71' }}>{success}</div>}

          {step === 1 ? (
            <form onSubmit={handleRequestToken} className="auth-form" autoComplete="off">
              <div className="input-group">
                <label className="input-label">{t('auth.emailYourLabel')}</label>
                <div className="auth-input-wrapper">
                  <FaEnvelope className="auth-icon" />
                  <input 
                    type="email" 
                    className="input-field" 
                    placeholder={t('auth.emailAddressPlaceholder')}
                    value={email}
                    autoComplete="email"
                    onChange={(e) => setEmail(e.target.value)}
                    required 
                  />
                </div>
              </div>

              <button type="submit" className="btn btn-primary auth-btn" disabled={loading}>
                {loading ? t('auth.sendingBtn') : t('auth.sendCodeBtn')}
              </button>
            </form>
          ) : (
            <form onSubmit={handleResetPassword} className="auth-form" autoComplete="off">
              <div className="input-group">
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '5px' }}>
                  <label className="input-label" style={{ marginBottom: 0 }}>{t('auth.tokenLabel')}</label>
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
                    {countdown > 0 ? t('auth.resendCodeIn', { s: countdown }) : t('auth.resendCode')}
                  </button>
                </div>
                <div className="auth-input-wrapper">
                  <FaKey className="auth-icon" />
                  <input 
                    type="text" 
                    className="input-field" 
                    placeholder={t('auth.tokenPlaceholder')}
                    value={token}
                    onChange={(e) => setToken(e.target.value)}
                    autoComplete="one-time-code"
                    required 
                  />
                </div>
              </div>

              <div className="input-group">
                <label className="input-label">{t('auth.newPasswordLabel')}</label>
                <div className="auth-input-wrapper">
                  <FaLock className="auth-icon" />
                  <input 
                    type="password" 
                    className="input-field" 
                    placeholder={t('auth.newPasswordPlaceholder')}
                    value={newPassword}
                    onChange={(e) => setNewPassword(e.target.value)}
                    autoComplete="new-password"
                    required 
                  />
                </div>
                {isPasswordShort && <span style={{color: '#e74c3c', fontSize: '13px', marginTop: '4px', display: 'block', textAlign: 'left', fontWeight: '500'}}>{t('auth.passwordShort')}</span>}
              </div>
              
              <div className="input-group">
                <label className="input-label">{t('auth.confirmNewPasswordLabel')}</label>
                <div className="auth-input-wrapper">
                  <FaLock className="auth-icon" />
                  <input 
                    type="password" 
                    className="input-field" 
                    placeholder={t('auth.confirmNewPasswordPlaceholder')}
                    value={confirmPassword}
                    onChange={(e) => setConfirmPassword(e.target.value)}
                    autoComplete="new-password"
                    required 
                  />
                </div>
                {isPasswordMismatch && <span style={{color: '#e74c3c', fontSize: '13px', marginTop: '4px', display: 'block', textAlign: 'left', fontWeight: '500'}}>{t('auth.passwordMismatch')}</span>}
              </div>

              <button type="submit" className="btn btn-primary auth-btn" disabled={loading || isFormInvalid}>
                {loading ? t('auth.processing') : t('auth.resetPasswordBtn')}
              </button>
            </form>
          )}

          <div className="auth-footer" style={{ marginTop: '24px' }}>
            <Link to="/login" className="auth-link" style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '5px' }}>
              <FaArrowLeft /> {t('auth.backToLogin')}
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ForgotPassword;
