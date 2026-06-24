import React from 'react';
import '../styles/pages.css';
import { useTranslation } from 'react-i18next';

const TermsOfServicePage: React.FC = () => {
  const { t } = useTranslation();

  return (
    <div className="page-wrapper" style={{ paddingTop: '100px', minHeight: '100vh', background: 'var(--bg-alt)' }}>
      <div className="container" style={{ background: 'white', padding: '40px', borderRadius: '16px', boxShadow: 'var(--shadow-sm)', marginBottom: '40px' }}>
        <h1 style={{ marginBottom: '32px', color: 'var(--primary-dark)', fontSize: '2.5rem' }}>{t('terms.title')}</h1>
        
        <div style={{ marginBottom: '24px', color: 'var(--text-secondary)', lineHeight: '1.8' }}>
          <p>{t('terms.intro')}</p>
          
          <h3 style={{ marginTop: '24px', marginBottom: '12px', color: 'var(--text-primary)' }}>{t('terms.h1')}</h3>
          <p>{t('terms.p1')}</p>

          <h3 style={{ marginTop: '24px', marginBottom: '12px', color: 'var(--text-primary)' }}>{t('terms.h2')}</h3>
          <p>{t('terms.p2')}</p>

          <h3 style={{ marginTop: '24px', marginBottom: '12px', color: 'var(--text-primary)' }}>{t('terms.h3')}</h3>
          <p>{t('terms.p3')}</p>
        </div>
      </div>
    </div>
  );
};

export default TermsOfServicePage;
