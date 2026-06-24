import React from 'react';
import '../styles/pages.css';
import { useTranslation } from 'react-i18next';

const PrivacyPolicyPage: React.FC = () => {
  const { t } = useTranslation();

  return (
    <div className="page-wrapper" style={{ paddingTop: '100px', minHeight: '100vh', background: 'var(--bg-alt)' }}>
      <div className="container" style={{ background: 'white', padding: '40px', borderRadius: '16px', boxShadow: 'var(--shadow-sm)', marginBottom: '40px' }}>
        <h1 style={{ marginBottom: '32px', color: 'var(--primary-dark)', fontSize: '2.5rem' }}>{t('privacy.title')}</h1>
        
        <div style={{ marginBottom: '24px', color: 'var(--text-secondary)', lineHeight: '1.8' }}>
          <p>{t('privacy.intro')}</p>
          
          <h3 style={{ marginTop: '24px', marginBottom: '12px', color: 'var(--text-primary)' }}>{t('privacy.h1')}</h3>
          <p>{t('privacy.p1')}</p>

          <h3 style={{ marginTop: '24px', marginBottom: '12px', color: 'var(--text-primary)' }}>{t('privacy.h2')}</h3>
          <p>{t('privacy.p2')}</p>
          <ul style={{ marginLeft: '20px', marginTop: '10px' }}>
            <li>{t('privacy.li1')}</li>
            <li>{t('privacy.li2')}</li>
            <li>{t('privacy.li3')}</li>
            <li>{t('privacy.li4')}</li>
          </ul>
        </div>
      </div>
    </div>
  );
};

export default PrivacyPolicyPage;
