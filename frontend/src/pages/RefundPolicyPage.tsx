import React from 'react';
import '../styles/pages.css';
import { useTranslation } from 'react-i18next';
import { FaUndoAlt, FaCheckCircle } from 'react-icons/fa';

const RefundPolicyPage: React.FC = () => {
  const { t } = useTranslation();

  return (
    <div className="page-wrapper" style={{ paddingTop: '100px', minHeight: '100vh', background: 'var(--bg-alt)' }}>
      <div className="container" style={{ maxWidth: '800px', margin: '0 auto', padding: '0 20px' }}>
        
        <div style={{ textAlign: 'center', marginBottom: '50px' }}>
          <div style={{ display: 'inline-flex', alignItems: 'center', justifyContent: 'center', width: '80px', height: '80px', borderRadius: '50%', background: 'rgba(37, 99, 235, 0.1)', color: 'var(--primary-dark)', marginBottom: '24px' }}>
            <FaUndoAlt size={40} />
          </div>
          <h1 style={{ color: 'var(--primary-dark)', fontSize: '3rem', fontWeight: '800', marginBottom: '16px' }}>{t('profile.refund.title')}</h1>
          <p style={{ color: 'var(--text-secondary)', fontSize: '1.2rem', maxWidth: '600px', margin: '0 auto', lineHeight: '1.6' }}>
            Chúng tôi luôn muốn bạn hoàn toàn hài lòng. Vui lòng xem qua chính sách hoàn tiền chi tiết.
          </p>
        </div>

        <div style={{ background: 'white', borderRadius: '24px', boxShadow: '0 20px 40px rgba(0,0,0,0.05)', padding: '50px', marginBottom: '80px', color: 'var(--text-secondary)', lineHeight: '1.8', fontSize: '1.05rem' }}>
          <p style={{ fontSize: '1.1rem', marginBottom: '30px' }}>{t('profile.refund.intro')}</p>
          
          <h3 style={{ marginTop: '40px', marginBottom: '20px', color: 'var(--text-primary)', fontSize: '1.5rem', fontWeight: '700', display: 'flex', alignItems: 'center', gap: '10px' }}>
            <span style={{ width: '8px', height: '24px', background: 'var(--primary-color)', borderRadius: '4px', display: 'inline-block' }}></span>
            {t('profile.refund.h1')}
          </h3>
          <div style={{ background: 'var(--bg-alt)', padding: '24px', borderRadius: '16px', marginBottom: '20px' }}>
            <ul style={{ listStyle: 'none', padding: 0, margin: 0, display: 'flex', flexDirection: 'column', gap: '16px' }}>
              <li style={{ display: 'flex', alignItems: 'flex-start', gap: '12px' }}><FaCheckCircle style={{ color: 'var(--success-color)', marginTop: '4px', flexShrink: 0 }} /> {t('profile.refund.li1')}</li>
              <li style={{ display: 'flex', alignItems: 'flex-start', gap: '12px' }}><FaCheckCircle style={{ color: 'var(--warning-color, #f59e0b)', marginTop: '4px', flexShrink: 0 }} /> {t('profile.refund.li2')}</li>
              <li style={{ display: 'flex', alignItems: 'flex-start', gap: '12px' }}><FaCheckCircle style={{ color: 'var(--danger-color, #ef4444)', marginTop: '4px', flexShrink: 0 }} /> {t('profile.refund.li3')}</li>
            </ul>
          </div>

          <h3 style={{ marginTop: '40px', marginBottom: '20px', color: 'var(--text-primary)', fontSize: '1.5rem', fontWeight: '700', display: 'flex', alignItems: 'center', gap: '10px' }}>
            <span style={{ width: '8px', height: '24px', background: 'var(--primary-color)', borderRadius: '4px', display: 'inline-block' }}></span>
            {t('profile.refund.h2')}
          </h3>
          <p style={{ marginBottom: '20px' }}>{t('profile.refund.p2')}</p>

          <h3 style={{ marginTop: '40px', marginBottom: '20px', color: 'var(--text-primary)', fontSize: '1.5rem', fontWeight: '700', display: 'flex', alignItems: 'center', gap: '10px' }}>
            <span style={{ width: '8px', height: '24px', background: 'var(--primary-color)', borderRadius: '4px', display: 'inline-block' }}></span>
            {t('profile.refund.h3')}
          </h3>
          <p style={{ marginBottom: '20px', padding: '20px', background: '#f8fafc', borderLeft: '4px solid var(--primary-color)', borderRadius: '0 8px 8px 0' }}>{t('profile.refund.p3')}</p>
        </div>
      </div>
    </div>
  );
};

export default RefundPolicyPage;
