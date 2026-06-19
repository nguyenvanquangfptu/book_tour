import React, { useState } from 'react';
import { FaEye, FaEyeSlash } from 'react-icons/fa';
import { UserService } from '../../services/UserService';
import { useMutation } from '@tanstack/react-query';

interface ChangePasswordTabProps {
  setMessage: (msg: { text: string; type: string }) => void;
}

const ChangePasswordTab: React.FC<ChangePasswordTabProps> = ({ setMessage }) => {
  const [passwords, setPasswords] = useState({ oldPassword: '', newPassword: '', confirmPassword: '' });
  const [showOldPass, setShowOldPass] = useState(false);
  const [showNewPass, setShowNewPass] = useState(false);
  const [showConfirmPass, setShowConfirmPass] = useState(false);

  const changePasswordMutation = useMutation({
    mutationFn: (data: any) => UserService.changePassword(data),
    onSuccess: () => {
      setMessage({ text: 'Đổi mật khẩu thành công!', type: 'success' });
      setPasswords({ oldPassword: '', newPassword: '', confirmPassword: '' });
    },
    onError: (error: any) => {
      setMessage({ text: error.response?.data?.message || 'Đổi mật khẩu thất bại!', type: 'error' });
    }
  });

  const handlePasswordSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (passwords.newPassword.length < 8) {
      setMessage({ text: 'Mật khẩu mới phải có ít nhất 8 ký tự!', type: 'error' });
      return;
    }
    if (passwords.newPassword !== passwords.confirmPassword) {
      setMessage({ text: 'Mật khẩu xác nhận không khớp!', type: 'error' });
      return;
    }
    changePasswordMutation.mutate(passwords);
  };

  const isPasswordShort = passwords.newPassword.length > 0 && passwords.newPassword.length < 8;
  const isPasswordMismatch = passwords.confirmPassword.length > 0 && passwords.newPassword !== passwords.confirmPassword;
  const isFormInvalid = isPasswordShort || isPasswordMismatch;

  return (
    <div>
      <h2 style={{ marginBottom: '5px', color: '#0f172a' }}>Đổi Mật Khẩu</h2>
      <p style={{ color: '#64748b', fontSize: '0.9rem', marginBottom: '20px' }}>
        Mật khẩu mới nên có ít nhất 8 ký tự, gồm chữ hoa, chữ thường và số.
      </p>
      <form onSubmit={handlePasswordSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '15px', maxWidth: '400px' }} autoComplete="off">
        <div className="input-group">
          <label className="input-label">Mật khẩu hiện tại</label>
          <div style={{ position: 'relative' }}>
            <input type={showOldPass ? "text" : "password"} className="input-field" value={passwords.oldPassword} onChange={e => setPasswords({...passwords, oldPassword: e.target.value})} autoComplete="new-password" required style={{ paddingRight: '40px', width: '100%' }} />
            <button type="button" onClick={() => setShowOldPass(!showOldPass)} style={{ position: 'absolute', right: '10px', top: '50%', transform: 'translateY(-50%)', background: 'none', border: 'none', color: '#64748b', cursor: 'pointer' }}>
              {showOldPass ? <FaEyeSlash /> : <FaEye />}
            </button>
          </div>
        </div>
        <div className="input-group">
          <label className="input-label">Mật khẩu mới</label>
          <div style={{ position: 'relative' }}>
            <input type={showNewPass ? "text" : "password"} className="input-field" value={passwords.newPassword} onChange={e => setPasswords({...passwords, newPassword: e.target.value})} autoComplete="new-password" required style={{ paddingRight: '40px', width: '100%' }} />
            <button type="button" onClick={() => setShowNewPass(!showNewPass)} style={{ position: 'absolute', right: '10px', top: '50%', transform: 'translateY(-50%)', background: 'none', border: 'none', color: '#64748b', cursor: 'pointer' }}>
              {showNewPass ? <FaEyeSlash /> : <FaEye />}
            </button>
          </div>
          {isPasswordShort && <span style={{color: '#e74c3c', fontSize: '13px', marginTop: '4px', display: 'block', fontWeight: '500'}}>Mật khẩu phải có ít nhất 8 ký tự.</span>}
        </div>
        <div className="input-group">
          <label className="input-label">Xác nhận mật khẩu mới</label>
          <div style={{ position: 'relative' }}>
            <input type={showConfirmPass ? "text" : "password"} className="input-field" value={passwords.confirmPassword} onChange={e => setPasswords({...passwords, confirmPassword: e.target.value})} autoComplete="new-password" required style={{ paddingRight: '40px', width: '100%' }} />
            <button type="button" onClick={() => setShowConfirmPass(!showConfirmPass)} style={{ position: 'absolute', right: '10px', top: '50%', transform: 'translateY(-50%)', background: 'none', border: 'none', color: '#64748b', cursor: 'pointer' }}>
              {showConfirmPass ? <FaEyeSlash /> : <FaEye />}
            </button>
          </div>
          {isPasswordMismatch && <span style={{color: '#e74c3c', fontSize: '13px', marginTop: '4px', display: 'block', fontWeight: '500'}}>Mật khẩu xác nhận không khớp.</span>}
        </div>
        <button type="submit" className="btn btn-primary" disabled={changePasswordMutation.isPending || isFormInvalid} style={{ alignSelf: 'flex-start', marginTop: '10px' }}>
          {changePasswordMutation.isPending ? 'Đang đổi mật khẩu...' : 'Cập nhật mật khẩu'}
        </button>
      </form>
    </div>
  );
};

export default ChangePasswordTab;
