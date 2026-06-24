import React, { useState, useEffect } from 'react';
import { FaUser } from 'react-icons/fa';
import api from '../../api/axiosConfig';
import { UserService } from '../../services/UserService';
import { useAuthStore } from '../../store/useAuthStore';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';

interface UserInfoTabProps {
  initialProfile: any;
  setMessage: (msg: { text: string; type: string }) => void;
}

const UserInfoTab: React.FC<UserInfoTabProps> = ({ initialProfile, setMessage }) => {
  const { t } = useTranslation();
  const queryClient = useQueryClient();
  const [profile, setProfile] = useState({ fullName: '', email: '', phone: '', avatar: '' });
  const [uploadingAvatar, setUploadingAvatar] = useState(false);

  useEffect(() => {
    if (initialProfile) {
      setProfile({
        fullName: initialProfile.fullName || '',
        email: initialProfile.email || '',
        phone: initialProfile.phone || '',
        avatar: initialProfile.avatar || ''
      });
    }
  }, [initialProfile]);

  const updateProfileMutation = useMutation({
    mutationFn: (data: any) => UserService.updateProfile(data),
    onSuccess: () => {
      setMessage({ text: t('profile.userInfo.successMsg'), type: 'success' });
      queryClient.invalidateQueries({ queryKey: ['profile'] });
    },
    onError: (error: any) => {
      setMessage({ text: error.response?.data?.message || t('profile.userInfo.errorMsg'), type: 'error' });
    }
  });

  const handleAvatarUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    if (!e.target.files || e.target.files.length === 0) return;
    const file = e.target.files[0];
    const formData = new FormData();
    formData.append('file', file);
    setUploadingAvatar(true);
    try {
      const res = await api.post('/upload', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      });
      const url = res.data?.data?.url || res.data?.url || res.data;
      if (typeof url === 'string') {
        await UserService.updateAvatar(url);
        setProfile({ ...profile, avatar: url });
        setMessage({ text: t('profile.userInfo.successMsg'), type: 'success' });
        const user = useAuthStore.getState().user;
        if (user) {
          useAuthStore.setState({ user: { ...user, avatar: url } });
        }
        queryClient.invalidateQueries({ queryKey: ['profile'] });
      }
    } catch (err: any) {
      const errorMessage = err.response?.data?.message || t('profile.userInfo.errorMsg');
      setMessage({ text: errorMessage, type: 'error' });
    } finally {
      setUploadingAvatar(false);
    }
  };

  const handleProfileSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    updateProfileMutation.mutate(profile);
  };

  const isEmailInvalid = profile.email.length > 0 && !/^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.(com|vn|net|org|edu|gov|info|io)$/.test(profile.email);
  const isPhoneInvalid = profile.phone.length > 0 && !/^(0|\+84)[0-9]{9}$/.test(profile.phone);
  const isFormInvalid = isEmailInvalid || isPhoneInvalid;

  return (
    <div>
      <h2 style={{ marginBottom: '20px', color: '#0f172a' }}>{t('profile.userInfo.title')}</h2>
      <div style={{ display: 'flex', alignItems: 'center', gap: '20px', marginBottom: '20px' }}>
        <div style={{ width: '100px', height: '100px', borderRadius: '50%', background: '#f1f5f9', overflow: 'hidden', display: 'flex', justifyContent: 'center', alignItems: 'center', position: 'relative' }}>
          {profile.avatar ? (
            <img src={profile.avatar} alt="Avatar" style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
          ) : (
            <FaUser style={{ fontSize: '3rem', color: '#cbd5e1' }} />
          )}
        </div>
        <div>
          <input type="file" id="avatar-upload" style={{ display: 'none' }} accept="image/*" onChange={handleAvatarUpload} />
          <label htmlFor="avatar-upload" style={{ cursor: 'pointer', display: 'inline-block', padding: '8px 16px', background: '#f1f5f9', color: '#334155', borderRadius: '6px', fontWeight: 500, border: '1px solid #e2e8f0', transition: 'all 0.2s' }}>
            {uploadingAvatar ? 'Đang tải...' : 'Thay đổi ảnh'}
          </label>
          <p style={{ margin: '5px 0 0', fontSize: '0.8rem', color: '#94a3b8' }}>Chấp nhận JPG, PNG hoặc GIF</p>
        </div>
      </div>
      <form onSubmit={handleProfileSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '15px' }} autoComplete="off">
        <div className="input-group">
          <label className="input-label">{t('profile.userInfo.fullName')}</label>
          <input type="text" className="input-field" value={profile.fullName} onChange={e => setProfile({...profile, fullName: e.target.value})} autoComplete="off" required />
        </div>
        <div className="input-group">
          <label className="input-label">{t('profile.userInfo.email')}</label>
          <input type="email" className="input-field" value={profile.email} onChange={e => setProfile({...profile, email: e.target.value})} pattern="^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.(com|vn|net|org|edu|gov|info|io)$" title="Vui lòng nhập đúng định dạng email hợp lệ" autoComplete="off" required />
          {isEmailInvalid && <span style={{color: '#e74c3c', fontSize: '13px', marginTop: '4px', display: 'block', fontWeight: '500'}}>Vui lòng nhập email hợp lệ (vd: abc@gmail.com).</span>}
        </div>
        <div className="input-group">
          <label className="input-label">{t('profile.userInfo.phone')}</label>
          <input type="text" className="input-field" value={profile.phone} onChange={e => setProfile({...profile, phone: e.target.value})} pattern="^(0|\+84)[0-9]{9}$" title="Vui lòng nhập đúng số điện thoại Việt Nam" autoComplete="off" required />
          {isPhoneInvalid && <span style={{color: '#e74c3c', fontSize: '13px', marginTop: '4px', display: 'block', fontWeight: '500'}}>Vui lòng nhập đúng số điện thoại (vd: 0912345678).</span>}
        </div>
        <button type="submit" className="btn btn-primary" disabled={updateProfileMutation.isPending || isFormInvalid} style={{ alignSelf: 'flex-start', marginTop: '10px' }}>
          {updateProfileMutation.isPending ? t('profile.userInfo.savingBtn') : t('profile.userInfo.saveBtn')}
        </button>
      </form>
    </div>
  );
};

export default UserInfoTab;
