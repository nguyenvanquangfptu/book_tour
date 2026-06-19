import React from 'react';
import { FaFileInvoiceDollar, FaDownload } from 'react-icons/fa';
import { formatPrice } from '../../utils/formatPrice';

interface BillingTabProps {
  bookings: any[];
  triggerPrint: (booking: any) => void;
  isGeneratingPDF: boolean;
}

const BillingTab: React.FC<BillingTabProps> = ({ bookings, triggerPrint, isGeneratingPDF }) => {
  const paidBookings = bookings.filter(b => b.status === 'PAID');

  return (
    <div>
      <h2 style={{ marginBottom: '20px', color: '#0f172a' }}>Hóa Đơn Của Tôi</h2>
      {paidBookings.length === 0 ? (
        <div style={{ padding: '40px', textAlign: 'center', background: '#f8fafc', borderRadius: '12px', border: '1px dashed #cbd5e1' }}>
          <FaFileInvoiceDollar style={{ fontSize: '3rem', color: '#94a3b8', marginBottom: '15px' }} />
          <h3 style={{ color: '#475569', marginBottom: '10px' }}>Chưa có hóa đơn nào</h3>
          <p style={{ color: '#64748b' }}>Các hóa đơn và biên lai thanh toán của bạn sẽ hiển thị tại đây.</p>
        </div>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '15px' }}>
          {paidBookings.map((booking, idx) => (
            <div key={idx} style={{ padding: '20px', border: '1px solid #e2e8f0', borderRadius: '8px', display: 'flex', flexWrap: 'wrap', gap: '20px', justifyContent: 'space-between', alignItems: 'center', background: '#f8fafc' }}>
              <div style={{ display: 'flex', gap: '20px', alignItems: 'center' }}>
                <div style={{ width: '50px', height: '50px', background: '#e0e7ff', borderRadius: '50%', display: 'flex', justifyContent: 'center', alignItems: 'center' }}>
                  <FaFileInvoiceDollar style={{ fontSize: '1.5rem', color: '#4338ca' }} />
                </div>
                <div>
                  <h3 style={{ margin: '0 0 5px 0', fontSize: '1.1rem', color: '#1e293b' }}>Hóa đơn #{booking.id}</h3>
                  <p style={{ margin: '0 0 5px 0', color: '#475569', fontSize: '0.9rem' }}>Ngày thanh toán: {new Date(booking.bookingDate).toLocaleDateString('vi-VN')}</p>
                  <p style={{ margin: '0', color: '#e11d48', fontWeight: 'bold' }}>{formatPrice(booking.totalPrice)}</p>
                </div>
              </div>
              <div style={{ textAlign: 'right' }}>
                <span style={{ padding: '5px 12px', borderRadius: '20px', fontSize: '0.85rem', fontWeight: '600', background: '#dcfce7', color: '#15803d', display: 'inline-block', marginBottom: '10px' }}>
                  Thành Công
                </span>
                <br />
                <button 
                  onClick={() => triggerPrint(booking)}
                  disabled={isGeneratingPDF}
                  style={{ background: 'transparent', border: '1px solid #cbd5e1', padding: '6px 12px', borderRadius: '6px', fontSize: '0.85rem', cursor: isGeneratingPDF ? 'not-allowed' : 'pointer', color: '#475569', transition: 'all 0.2s', display: 'inline-flex', alignItems: 'center', gap: '5px' }}
                  onMouseEnter={(e) => { if(!isGeneratingPDF) { e.currentTarget.style.background = '#f1f5f9'; e.currentTarget.style.borderColor = '#94a3b8'; } }}
                  onMouseLeave={(e) => { if(!isGeneratingPDF) { e.currentTarget.style.background = 'transparent'; e.currentTarget.style.borderColor = '#cbd5e1'; } }}
                >
                  <FaDownload /> {isGeneratingPDF ? 'Đang tải...' : 'Tải Biên Lai PDF'}
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default BillingTab;
