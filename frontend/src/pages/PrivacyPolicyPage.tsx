import React from 'react';
import '../styles/pages.css';
import { useTranslation } from 'react-i18next';
import { FaShieldAlt, FaCheckCircle } from 'react-icons/fa';

const PrivacyPolicyPage: React.FC = () => {
  const { t } = useTranslation();

  return (
    <div className="page-wrapper" style={{ paddingTop: '100px', minHeight: '100vh', background: 'var(--bg-alt)' }}>
      <div className="container" style={{ maxWidth: '800px', margin: '0 auto', padding: '0 20px' }}>
        
        <div style={{ textAlign: 'center', marginBottom: '50px' }}>
          <div style={{ display: 'inline-flex', alignItems: 'center', justifyContent: 'center', width: '80px', height: '80px', borderRadius: '50%', background: 'rgba(37, 99, 235, 0.1)', color: 'var(--primary-dark)', marginBottom: '24px' }}>
            <FaShieldAlt size={40} />
          </div>
          <h1 style={{ color: 'var(--primary-dark)', fontSize: '3rem', fontWeight: '800', marginBottom: '16px' }}>{t('profile.privacy.title')}</h1>
          <p style={{ color: 'var(--text-secondary)', fontSize: '1.2rem', maxWidth: '600px', margin: '0 auto', lineHeight: '1.6' }}>
            Chúng tôi cam kết bảo vệ thông tin cá nhân và quyền riêng tư của bạn.
          </p>
        </div>

        <div style={{ background: 'white', borderRadius: '24px', boxShadow: '0 20px 40px rgba(0,0,0,0.05)', padding: '50px', marginBottom: '80px', color: 'var(--text-secondary)', lineHeight: '1.8', fontSize: '1.05rem' }}>
          <p style={{ fontSize: '1.1rem', marginBottom: '30px' }}>{t('profile.privacy.intro')}</p>
          
          <h3 style={{ marginTop: '40px', marginBottom: '20px', color: 'var(--text-primary)', fontSize: '1.5rem', fontWeight: '700', display: 'flex', alignItems: 'center', gap: '10px' }}>
            <span style={{ width: '8px', height: '24px', background: 'var(--primary-color)', borderRadius: '4px', display: 'inline-block' }}></span>
            {t('profile.privacy.h1')}
          </h3>
          <p style={{ marginBottom: '20px' }}>{t('profile.privacy.p1')}</p>

          <h3 style={{ marginTop: '40px', marginBottom: '20px', color: 'var(--text-primary)', fontSize: '1.5rem', fontWeight: '700', display: 'flex', alignItems: 'center', gap: '10px' }}>
            <span style={{ width: '8px', height: '24px', background: 'var(--primary-color)', borderRadius: '4px', display: 'inline-block' }}></span>
            {t('profile.privacy.h2')}
          </h3>
          <p style={{ marginBottom: '20px' }}>{t('profile.privacy.p2')}</p>
          
          <div style={{ background: 'var(--bg-alt)', padding: '24px', borderRadius: '16px', marginTop: '20px' }}>
            <ul style={{ listStyle: 'none', padding: 0, margin: 0, display: 'flex', flexDirection: 'column', gap: '16px' }}>
              <li style={{ display: 'flex', alignItems: 'flex-start', gap: '12px' }}><FaCheckCircle style={{ color: 'var(--success-color)', marginTop: '4px', flexShrink: 0 }} /> {t('profile.privacy.li1')}</li>
              <li style={{ display: 'flex', alignItems: 'flex-start', gap: '12px' }}><FaCheckCircle style={{ color: 'var(--success-color)', marginTop: '4px', flexShrink: 0 }} /> {t('profile.privacy.li2')}</li>
              <li style={{ display: 'flex', alignItems: 'flex-start', gap: '12px' }}><FaCheckCircle style={{ color: 'var(--success-color)', marginTop: '4px', flexShrink: 0 }} /> {t('profile.privacy.li3')}</li>
              <li style={{ display: 'flex', alignItems: 'flex-start', gap: '12px' }}><FaCheckCircle style={{ color: 'var(--success-color)', marginTop: '4px', flexShrink: 0 }} /> {t('profile.privacy.li4')}</li>
            </ul>
          </div>
        </div>
      </div>
    </div>
  );
};

export default PrivacyPolicyPage;
