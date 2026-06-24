import React from 'react';
import { FaFileInvoiceDollar, FaMap, FaMapMarkerAlt, FaCalendarAlt, FaUsers, FaIdCard, FaUser, FaPhone, FaEnvelope, FaStickyNote, FaCheckCircle, FaMoneyBillWave } from 'react-icons/fa';
import { useNavigate } from 'react-router-dom';
import { formatPrice } from '../../utils/formatPrice';
import { useTranslation } from 'react-i18next';

interface BookingDetailsModalProps {
  selectedBooking: any;
  setSelectedBooking: (booking: any | null) => void;
  profile: any;
}

const BookingDetailsModal: React.FC<BookingDetailsModalProps> = ({ selectedBooking, setSelectedBooking, profile }) => {
  const navigate = useNavigate();
  const { t } = useTranslation();

  if (!selectedBooking) return null;

  return (
    <div style={{ position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.5)', display: 'flex', justifyContent: 'center', alignItems: 'center', zIndex: 1000, padding: '20px' }} onClick={() => setSelectedBooking(null)}>
      <div style={{ background: '#f8fafc', borderRadius: '16px', width: '100%', maxWidth: '650px', position: 'relative', boxShadow: '0 25px 50px -12px rgba(0, 0, 0, 0.25)', overflow: 'hidden', display: 'flex', flexDirection: 'column', maxHeight: '90vh' }} onClick={e => e.stopPropagation()}>
        {/* Header */}
        <div style={{ background: 'linear-gradient(135deg, #1e293b 0%, #0f172a 100%)', padding: '25px 30px', position: 'relative' }}>
          <button onClick={() => setSelectedBooking(null)} style={{ position: 'absolute', top: '20px', right: '20px', background: 'rgba(255,255,255,0.1)', border: 'none', width: '32px', height: '32px', borderRadius: '50%', display: 'flex', alignItems: 'center', justifyContent: 'center', cursor: 'pointer', color: '#fff', transition: 'all 0.2s' }} onMouseOver={e => e.currentTarget.style.background = 'rgba(255,255,255,0.2)'} onMouseOut={e => e.currentTarget.style.background = 'rgba(255,255,255,0.1)'}>&times;</button>
          <div style={{ display: 'flex', alignItems: 'center', gap: '15px' }}>
            <div style={{ background: 'rgba(255,255,255,0.15)', padding: '12px', borderRadius: '12px', color: '#38bdf8' }}>
              <FaFileInvoiceDollar size={24} />
            </div>
            <div>
              <h2 style={{ color: '#fff', margin: 0, fontSize: '1.5rem', fontWeight: '700' }}>{t('profile.bookingModal.title')} #{selectedBooking.id}</h2>
              <p style={{ color: '#94a3b8', margin: '5px 0 0 0', fontSize: '0.9rem' }}>{t('profile.bookingHistory.date')}: {new Date(selectedBooking.bookingDate).toLocaleString('vi-VN')}</p>
            </div>
          </div>
        </div>
        
        {/* Body */}
        <div style={{ padding: '30px', overflowY: 'auto' }}>
          
          {/* Tour Info Card */}
          <div style={{ background: '#fff', borderRadius: '12px', padding: '20px', boxShadow: '0 4px 6px -1px rgba(0,0,0,0.05), 0 2px 4px -1px rgba(0,0,0,0.03)', marginBottom: '20px', border: '1px solid #e2e8f0' }}>
            <h3 style={{ margin: '0 0 15px 0', color: '#0f172a', fontSize: '1.1rem', display: 'flex', alignItems: 'center', gap: '8px', borderBottom: '1px solid #f1f5f9', paddingBottom: '10px' }}>
              <FaMap color="#3b82f6" /> {t('profile.bookingModal.tourInfo')}
            </h3>
            
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '15px' }}>
              <div style={{ display: 'flex', alignItems: 'flex-start', gap: '10px' }}>
                <div style={{ color: '#64748b', marginTop: '3px' }}><FaMapMarkerAlt /></div>
                <div>
                  <div style={{ fontSize: '0.85rem', color: '#64748b', marginBottom: '2px' }}>{t('profile.bookingHistory.tour')}</div>
                  <div style={{ color: '#0f172a', fontWeight: '600' }}>{selectedBooking.tourTitle || t('profile.bookingModal.none')}</div>
                </div>
              </div>
              <div style={{ display: 'flex', alignItems: 'flex-start', gap: '10px' }}>
                <div style={{ color: '#64748b', marginTop: '3px' }}><FaMapMarkerAlt /></div>
                <div>
                  <div style={{ fontSize: '0.85rem', color: '#64748b', marginBottom: '2px' }}>{t('profile.bookingModal.none')}</div>
                  <div style={{ color: '#0f172a', fontWeight: '500' }}>{selectedBooking.destination || t('profile.bookingModal.none')}</div>
                </div>
              </div>
              <div style={{ display: 'flex', alignItems: 'flex-start', gap: '10px' }}>
                <div style={{ color: '#64748b', marginTop: '3px' }}><FaCalendarAlt /></div>
                <div>
                  <div style={{ fontSize: '0.85rem', color: '#64748b', marginBottom: '2px' }}>{t('profile.bookingModal.departure')}</div>
                  <div style={{ color: '#0f172a', fontWeight: '500' }}>{selectedBooking.travelDate ? new Date(selectedBooking.travelDate).toLocaleDateString('vi-VN') : 'N/A'}</div>
                </div>
              </div>
              <div style={{ display: 'flex', alignItems: 'flex-start', gap: '10px' }}>
                <div style={{ color: '#64748b', marginTop: '3px' }}><FaUsers /></div>
                <div>
                  <div style={{ fontSize: '0.85rem', color: '#64748b', marginBottom: '2px' }}>{t('profile.bookingModal.guests')}</div>
                  <div style={{ color: '#0f172a', fontWeight: '500' }}>{selectedBooking.numberOfPeople} {t('profile.bookingHistory.people')}</div>
                </div>
              </div>
            </div>
          </div>

          {/* Customer Info Card */}
          <div style={{ background: '#fff', borderRadius: '12px', padding: '20px', boxShadow: '0 4px 6px -1px rgba(0,0,0,0.05), 0 2px 4px -1px rgba(0,0,0,0.03)', marginBottom: '20px', border: '1px solid #e2e8f0' }}>
            <h3 style={{ margin: '0 0 15px 0', color: '#0f172a', fontSize: '1.1rem', display: 'flex', alignItems: 'center', gap: '8px', borderBottom: '1px solid #f1f5f9', paddingBottom: '10px' }}>
              <FaIdCard color="#8b5cf6" /> {t('profile.bookingModal.customerInfo')}
            </h3>
            
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '15px' }}>
              <div style={{ display: 'flex', alignItems: 'flex-start', gap: '10px' }}>
                <div style={{ color: '#64748b', marginTop: '3px' }}><FaUser /></div>
                <div>
                  <div style={{ fontSize: '0.85rem', color: '#64748b', marginBottom: '2px' }}>{t('profile.bookingModal.fullName')}</div>
                  <div style={{ color: '#0f172a', fontWeight: '500' }}>{selectedBooking.customerName || profile.fullName}</div>
                </div>
              </div>
              <div style={{ display: 'flex', alignItems: 'flex-start', gap: '10px' }}>
                <div style={{ color: '#64748b', marginTop: '3px' }}><FaPhone /></div>
                <div>
                  <div style={{ fontSize: '0.85rem', color: '#64748b', marginBottom: '2px' }}>{t('profile.bookingModal.phone')}</div>
                  <div style={{ color: '#0f172a', fontWeight: '500' }}>{selectedBooking.customerPhone || profile.phone || t('profile.bookingModal.none')}</div>
                </div>
              </div>
              <div style={{ display: 'flex', alignItems: 'flex-start', gap: '10px', gridColumn: '1 / -1' }}>
                <div style={{ color: '#64748b', marginTop: '3px' }}><FaEnvelope /></div>
                <div>
                  <div style={{ fontSize: '0.85rem', color: '#64748b', marginBottom: '2px' }}>{t('profile.bookingModal.email')}</div>
                  <div style={{ color: '#0f172a', fontWeight: '500' }}>{selectedBooking.customerEmail || profile.email || t('profile.bookingModal.none')}</div>
                </div>
              </div>
              {selectedBooking.note && (
                <div style={{ display: 'flex', alignItems: 'flex-start', gap: '10px', gridColumn: '1 / -1', background: '#f8fafc', padding: '10px', borderRadius: '8px' }}>
                  <div style={{ color: '#64748b', marginTop: '3px' }}><FaStickyNote /></div>
                  <div>
                    <div style={{ fontSize: '0.85rem', color: '#64748b', marginBottom: '2px' }}>{t('profile.bookingModal.note')}</div>
                    <div style={{ color: '#334155', fontStyle: 'italic', fontSize: '0.95rem' }}>"{selectedBooking.note}"</div>
                  </div>
                </div>
              )}
            </div>
          </div>
          
          {/* Payment Summary Card */}
          <div style={{ background: 'linear-gradient(to right, #eff6ff, #dbeafe)', borderRadius: '12px', padding: '20px', border: '1px solid #bfdbfe' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '15px' }}>
              <span style={{ color: '#1e3a8a', fontWeight: '600', display: 'flex', alignItems: 'center', gap: '8px' }}>
                <FaCheckCircle /> {t('profile.bookingModal.status')}
              </span>
              <span style={{ 
                fontWeight: 'bold', 
                padding: '6px 12px', 
                borderRadius: '20px', 
                fontSize: '0.9rem',
                background: selectedBooking.status === 'PENDING' ? '#fef3c7' : selectedBooking.status === 'CONFIRMED' ? '#dcfce7' : '#fee2e2',
                color: selectedBooking.status === 'PENDING' ? '#92400e' : selectedBooking.status === 'CONFIRMED' ? '#166534' : '#991b1b',
                border: `1px solid ${selectedBooking.status === 'PENDING' ? '#fde68a' : selectedBooking.status === 'CONFIRMED' ? '#bbf7d0' : '#fecaca'}`
              }}>
                {selectedBooking.status === 'PENDING' ? t('profile.bookingHistory.statusPending') : selectedBooking.status === 'CONFIRMED' ? t('profile.bookingHistory.statusApproved') : selectedBooking.status === 'PAID' ? t('profile.bookingHistory.statusPaid') : t('profile.bookingHistory.statusCancelled')}
              </span>
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderTop: '1px dashed #93c5fd', paddingTop: '15px' }}>
              <span style={{ color: '#1e3a8a', fontWeight: '700', fontSize: '1.2rem', display: 'flex', alignItems: 'center', gap: '8px' }}>
                <FaMoneyBillWave /> {t('profile.bookingModal.total')}:
              </span>
              <span style={{ color: '#be123c', fontWeight: '800', fontSize: '1.5rem', textShadow: '0 1px 1px rgba(0,0,0,0.05)' }}>
                {formatPrice(selectedBooking.totalPrice)}
              </span>
            </div>
          </div>

        </div>
        
        {/* Footer */}
        <div style={{ padding: '20px 30px', background: '#fff', borderTop: '1px solid #e2e8f0', display: 'flex', gap: '15px', justifyContent: 'flex-end', borderBottomLeftRadius: '16px', borderBottomRightRadius: '16px' }}>
          {selectedBooking.tourId && (
            <button 
              onClick={() => navigate(`/tours/${selectedBooking.tourId}`)}
              style={{ background: '#f8fafc', color: '#475569', border: '1px solid #cbd5e1', padding: '10px 20px', borderRadius: '8px', fontWeight: '600', cursor: 'pointer', transition: 'all 0.2s' }}
              onMouseOver={e => { e.currentTarget.style.background = '#f1f5f9'; e.currentTarget.style.color = '#0f172a'; }}
              onMouseOut={e => { e.currentTarget.style.background = '#f8fafc'; e.currentTarget.style.color = '#475569'; }}
            >
              {t('profile.bookingHistory.viewDetails')}
            </button>
          )}
          <button 
            onClick={() => setSelectedBooking(null)} 
            style={{ background: '#3b82f6', color: '#fff', border: 'none', padding: '10px 25px', borderRadius: '8px', fontWeight: '600', cursor: 'pointer', boxShadow: '0 4px 6px -1px rgba(59, 130, 246, 0.4)', transition: 'all 0.2s' }}
            onMouseOver={e => e.currentTarget.style.transform = 'translateY(-2px)'}
            onMouseOut={e => e.currentTarget.style.transform = 'translateY(0)'}
          >
            {t('profile.bookingModal.close')}
          </button>
        </div>
      </div>
    </div>
  );
};

export default BookingDetailsModal;
