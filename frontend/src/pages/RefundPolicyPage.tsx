import React from 'react';
import '../styles/pages.css';
import { useTranslation } from 'react-i18next';

const RefundPolicyPage: React.FC = () => {
  const { t } = useTranslation();

  return (
    <div className="page-wrapper" style={{ paddingTop: '100px', minHeight: '100vh', background: 'var(--bg-alt)' }}>
      <div className="container" style={{ background: 'white', padding: '40px', borderRadius: '16px', boxShadow: 'var(--shadow-sm)', marginBottom: '40px' }}>
        <h1 style={{ marginBottom: '32px', color: 'var(--primary-dark)', fontSize: '2.5rem' }}>{t('refund.title')}</h1>
        
        <div style={{ marginBottom: '24px', color: 'var(--text-secondary)', lineHeight: '1.8' }}>
          <p>{t('refund.intro')}</p>
          
          <h3 style={{ marginTop: '24px', marginBottom: '12px', color: 'var(--text-primary)' }}>{t('refund.h1')}</h3>
          <ul style={{ marginLeft: '20px', marginTop: '10px' }}>
            <li>{t('refund.li1')}</li>
            <li>{t('refund.li2')}</li>
            <li>{t('refund.li3')}</li>
          </ul>

          <h3 style={{ marginTop: '24px', marginBottom: '12px', color: 'var(--text-primary)' }}>{t('refund.h2')}</h3>
          <p>{t('refund.p2')}</p>

          <h3 style={{ marginTop: '24px', marginBottom: '12px', color: 'var(--text-primary)' }}>{t('refund.h3')}</h3>
          <p>{t('refund.p3')}</p>
        </div>
      </div>
    </div>
  );
};

export default RefundPolicyPage;
