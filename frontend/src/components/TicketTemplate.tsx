import React, { forwardRef } from 'react';
import { FaPlane, FaCalendarAlt, FaUserFriends, FaMapMarkerAlt, FaQrcode } from 'react-icons/fa';
import { formatPrice } from '../utils/formatPrice';

interface TicketProps {
  booking: any;
  profile: any;
}

const TicketTemplate = forwardRef<HTMLDivElement, TicketProps>(({ booking, profile }, ref) => {
  if (!booking) return null;

  return (
    <div 
      ref={ref} 
      style={{
        padding: '40px',
        background: '#fff',
        width: '800px',
        minHeight: '400px',
        margin: '0 auto',
        fontFamily: "'Inter', sans-serif",
        color: '#1e293b'
      }}
    >
      <div style={{ border: '2px dashed #cbd5e1', borderRadius: '16px', padding: '30px', position: 'relative', overflow: 'hidden' }}>
        {/* Ticket Header */}
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderBottom: '2px solid #f1f5f9', paddingBottom: '20px', marginBottom: '30px' }}>
          <div>
            <h1 style={{ margin: 0, color: '#2563eb', fontSize: '2rem', display: 'flex', alignItems: 'center', gap: '10px' }}>
              <FaPlane /> E-TICKET
            </h1>
            <p style={{ margin: '5px 0 0', color: '#64748b', fontSize: '0.9rem' }}>Chuyến đi tuyệt vời cùng Booking Tour</p>
          </div>
          <div style={{ textAlign: 'right' }}>
            <h2 style={{ margin: 0, fontSize: '1.2rem', color: '#0f172a' }}>MÃ VÉ</h2>
            <p style={{ margin: 0, fontSize: '1.5rem', fontWeight: 'bold', color: '#2563eb', letterSpacing: '2px' }}>
              #{String(booking.id).padStart(6, '0')}
            </p>
          </div>
        </div>

        {/* Passenger Info */}
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '30px', marginBottom: '30px' }}>
          <div>
            <p style={{ margin: '0 0 5px 0', fontSize: '0.85rem', color: '#64748b', textTransform: 'uppercase' }}>Hành Khách / Passenger</p>
            <p style={{ margin: 0, fontSize: '1.3rem', fontWeight: 'bold', color: '#0f172a' }}>{profile.fullName}</p>
            <p style={{ margin: '5px 0 0', fontSize: '1rem', color: '#475569' }}>{profile.phone}</p>
          </div>
          <div>
            <p style={{ margin: '0 0 5px 0', fontSize: '0.85rem', color: '#64748b', textTransform: 'uppercase' }}>Tour / Hành Trình</p>
            <p style={{ margin: 0, fontSize: '1.2rem', fontWeight: '600', color: '#0f172a', display: 'flex', alignItems: 'center', gap: '8px' }}>
              <FaMapMarkerAlt style={{ color: '#ef4444' }} /> {booking.tourTitle || 'Chuyến đi bí ẩn'}
            </p>
          </div>
        </div>

        {/* Journey Details */}
        <div style={{ display: 'flex', justifyContent: 'space-between', background: '#f8fafc', padding: '20px', borderRadius: '12px' }}>
          <div>
            <p style={{ margin: '0 0 5px 0', fontSize: '0.85rem', color: '#64748b', textTransform: 'uppercase', display: 'flex', alignItems: 'center', gap: '5px' }}>
              <FaCalendarAlt /> Ngày Đặt
            </p>
            <p style={{ margin: 0, fontSize: '1.1rem', fontWeight: '600' }}>
              {new Date(booking.bookingDate).toLocaleDateString('vi-VN')}
            </p>
          </div>
          <div>
            <p style={{ margin: '0 0 5px 0', fontSize: '0.85rem', color: '#64748b', textTransform: 'uppercase', display: 'flex', alignItems: 'center', gap: '5px' }}>
              <FaUserFriends /> Số Khách
            </p>
            <p style={{ margin: 0, fontSize: '1.1rem', fontWeight: '600' }}>
              {booking.numberOfPeople} Người
            </p>
          </div>
          <div>
            <p style={{ margin: '0 0 5px 0', fontSize: '0.85rem', color: '#64748b', textTransform: 'uppercase' }}>
              Tổng Tiền
            </p>
            <p style={{ margin: 0, fontSize: '1.1rem', fontWeight: 'bold', color: '#16a34a' }}>
              {formatPrice(booking.totalPrice)}
            </p>
          </div>
        </div>

        {/* Footer / QR Code dummy */}
        <div style={{ marginTop: '30px', borderTop: '2px dashed #f1f5f9', paddingTop: '20px', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <p style={{ color: '#94a3b8', fontSize: '0.85rem', margin: 0 }}>
            Vui lòng xuất trình vé này khi tham gia tour. Xin cảm ơn!
          </p>
          <div style={{ textAlign: 'center', color: '#cbd5e1' }}>
            <FaQrcode style={{ fontSize: '3rem' }} />
          </div>
        </div>

      </div>
    </div>
  );
});

export default TicketTemplate;
