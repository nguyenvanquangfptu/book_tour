import React, { useState } from 'react';
import { FaDownload, FaStar } from 'react-icons/fa';
import { formatPrice } from '../../utils/formatPrice';
import { BookingService } from '../../services/BookingService';
import api from '../../api/axiosConfig';
import { useQueryClient } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import Swal from 'sweetalert2';

interface BookingHistoryTabProps {
  bookings: any[];
  setSelectedBooking: (booking: any) => void;
  triggerPrint: (booking: any) => void;
  isGeneratingPDF: boolean;
  setMessage: (msg: { text: string; type: string }) => void;
}

const BookingHistoryTab: React.FC<BookingHistoryTabProps> = ({ 
  bookings, 
  setSelectedBooking, 
  triggerPrint, 
  isGeneratingPDF,
  setMessage
}) => {
  const { t } = useTranslation();
  const queryClient = useQueryClient();
  const [showReviewForm, setShowReviewForm] = useState<number | null>(null);
  const [reviewData, setReviewData] = useState({ rating: 5, comment: '' });
  const [loading, setLoading] = useState(false);

  const handleReviewSubmit = async (booking: any) => {
    try {
      setLoading(true);
      if (booking.reviewed) {
        await api.put(`/reviews/${booking.reviewId}`, { tourId: booking.tourId, rating: reviewData.rating, comment: reviewData.comment });
        setMessage({ text: t('profile.bookingHistory.reviewSuccess'), type: 'success' });
      } else {
        await api.post('/reviews', { tourId: booking.tourId, rating: reviewData.rating, comment: reviewData.comment });
        setMessage({ text: t('profile.bookingHistory.reviewThanks'), type: 'success' });
      }
      setShowReviewForm(null);
      queryClient.invalidateQueries({ queryKey: ['bookings'] });
    } catch(e: any) {
      setMessage({ text: e.response?.data?.message || t('profile.bookingHistory.reviewFailed'), type: 'error' });
    } finally {
      setLoading(false);
      window.scrollTo({ top: 0, behavior: 'smooth' });
    }
  };

  const handlePayment = async (bookingId: number) => {
    try {
      const res = await BookingService.createPayOSUrl(bookingId);
      const paymentData = res?.data || res;
      const url = typeof paymentData === 'string' ? paymentData : paymentData?.paymentUrl;
      if (url) {
        window.location.href = url;
      } else {
        Swal.fire('Lỗi', t('profile.bookingHistory.paymentUrlError'), 'error');
      }
    } catch(e: any) {
      console.error('Lỗi tạo link thanh toán:', e);
      const errorMsg = e.response?.data?.message || t('profile.bookingHistory.paymentCreateError');
      Swal.fire('Lỗi', errorMsg, 'error');
    }
  };

  return (
    <div>
      <h2 style={{ marginBottom: '20px', color: '#0f172a' }}>{t('profile.bookingHistory.title')}</h2>
      {bookings.length === 0 ? (
        <p style={{ color: '#64748b' }}>{t('profile.bookingHistory.noBookings')}</p>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '15px' }}>
          {bookings.map((booking, idx) => (
            <div key={idx} style={{ padding: '20px', border: '1px solid #e2e8f0', borderRadius: '8px', display: 'flex', flexWrap: 'wrap', gap: '20px', justifyContent: 'space-between', alignItems: 'center' }}>
              <div>
                <h3 style={{ margin: '0 0 5px 0', fontSize: '1.1rem', color: '#1e293b' }}>{t('profile.bookingHistory.bookingCode')}{booking.id}</h3>
                <p style={{ margin: '0 0 5px 0', color: '#475569' }}>{t('profile.bookingHistory.date')}: {new Date(booking.bookingDate).toLocaleDateString('vi-VN')}</p>
                <p style={{ margin: '0 0 5px 0', color: '#475569' }}>{t('profile.bookingHistory.quantity')}: {booking.numberOfPeople} {t('profile.bookingHistory.people')}</p>
                <p style={{ margin: '0', color: '#e11d48', fontWeight: 'bold' }}>{t('profile.bookingHistory.totalAmount')}: {formatPrice(booking.totalPrice)}</p>
              </div>
              <div style={{ textAlign: 'right', display: 'flex', flexDirection: 'column', gap: '10px', alignItems: 'flex-end' }}>
                <span style={{ 
                  padding: '5px 12px', 
                  borderRadius: '20px', 
                  fontSize: '0.85rem', 
                  fontWeight: '600',
                  background: booking.status === 'PENDING' ? '#fef3c7' : booking.status === 'CONFIRMED' ? '#e0e7ff' : booking.status === 'PAID' ? '#dcfce7' : '#fee2e2',
                  color: booking.status === 'PENDING' ? '#b45309' : booking.status === 'CONFIRMED' ? '#3730a3' : booking.status === 'PAID' ? '#15803d' : '#b91c1c'
                }}>
                  {booking.status === 'PENDING' ? t('profile.bookingHistory.statusPending') : booking.status === 'CONFIRMED' ? t('profile.bookingHistory.statusApproved') : booking.status === 'PAID' ? t('profile.bookingHistory.statusPaid') : t('profile.bookingHistory.statusCancelled')}
                </span>
                <button 
                  onClick={() => setSelectedBooking(booking)}
                  style={{ background: 'transparent', border: '1px solid #cbd5e1', padding: '6px 12px', borderRadius: '6px', fontSize: '0.85rem', cursor: 'pointer', color: '#475569', transition: 'all 0.2s' }}
                  onMouseEnter={(e) => { e.currentTarget.style.background = '#f1f5f9'; e.currentTarget.style.borderColor = '#94a3b8'; }}
                  onMouseLeave={(e) => { e.currentTarget.style.background = 'transparent'; e.currentTarget.style.borderColor = '#cbd5e1'; }}
                >
                  {t('profile.bookingHistory.viewDetails')}
                </button>
                {booking.status === 'CONFIRMED' && (
                  <button 
                    onClick={() => handlePayment(booking.id)}
                    style={{ background: '#10b981', border: 'none', padding: '6px 12px', borderRadius: '6px', color: '#fff', cursor: 'pointer', fontWeight: 'bold' }}
                  >
                    {t('profile.bookingHistory.payNow')}
                  </button>
                )}
                {booking.status === 'PAID' && (
                  <div style={{ display: 'flex', gap: '8px' }}>
                    <button 
                      onClick={() => triggerPrint(booking)}
                      disabled={isGeneratingPDF}
                      style={{ background: isGeneratingPDF ? '#94a3b8' : '#2563eb', color: '#fff', border: 'none', padding: '6px 12px', borderRadius: '6px', fontSize: '0.85rem', cursor: isGeneratingPDF ? 'not-allowed' : 'pointer', transition: 'all 0.2s', display: 'flex', alignItems: 'center', gap: '5px' }}
                      onMouseEnter={(e) => { if(!isGeneratingPDF) e.currentTarget.style.background = '#1d4ed8' }}
                      onMouseLeave={(e) => { if(!isGeneratingPDF) e.currentTarget.style.background = '#2563eb' }}
                    >
                      <FaDownload /> {isGeneratingPDF ? t('profile.bookingHistory.downloading') : t('profile.bookingHistory.downloadE_Ticket')}
                    </button>
                    <button 
                      onClick={() => { 
                        setShowReviewForm(booking.id); 
                        setReviewData({ 
                          rating: booking.reviewed ? booking.reviewRating : 5, 
                          comment: booking.reviewed ? booking.reviewComment : '' 
                        }); 
                      }}
                      style={{ background: booking.reviewed ? '#10b981' : '#f59e0b', color: '#fff', border: 'none', padding: '6px 12px', borderRadius: '6px', fontSize: '0.85rem', cursor: 'pointer', transition: 'all 0.2s' }}
                      onMouseEnter={(e) => e.currentTarget.style.background = booking.reviewed ? '#059669' : '#d97706'}
                      onMouseLeave={(e) => e.currentTarget.style.background = booking.reviewed ? '#10b981' : '#f59e0b'}
                    >
                      {booking.reviewed ? t('profile.bookingHistory.editReview') : t('profile.bookingHistory.reviewTour')}
                    </button>
                  </div>
                )}
              </div>
              
              {/* Review Inline Form */}
              {showReviewForm === booking.id && (
                <div style={{ width: '100%', marginTop: '15px', padding: '20px', background: '#f8fafc', borderRadius: '8px', border: '1px solid #e2e8f0', animation: 'fadeIn 0.3s ease-in-out' }}>
                  <h4 style={{ margin: '0 0 10px 0', color: '#0f172a' }}>{t('profile.bookingHistory.reviewTitle')}{booking.tourTitle}</h4>
                  <div style={{ display: 'flex', gap: '5px', marginBottom: '15px' }}>
                    {[1, 2, 3, 4, 5].map(star => (
                      <FaStar 
                        key={star} 
                        onClick={() => setReviewData({...reviewData, rating: star})}
                        style={{ cursor: 'pointer', color: star <= reviewData.rating ? '#f59e0b' : '#cbd5e1', fontSize: '1.5rem', transition: 'color 0.2s' }} 
                      />
                    ))}
                  </div>
                  <textarea 
                    placeholder={t('profile.bookingHistory.reviewPlaceholder')} 
                    value={reviewData.comment}
                    onChange={e => setReviewData({...reviewData, comment: e.target.value})}
                    style={{ width: '100%', padding: '12px', borderRadius: '8px', border: '1px solid #cbd5e1', minHeight: '100px', marginBottom: '15px', resize: 'vertical', fontFamily: 'inherit' }}
                  />
                  <div style={{ display: 'flex', gap: '10px' }}>
                    <button 
                      onClick={() => handleReviewSubmit(booking)}
                      className="btn btn-primary" 
                      style={{ padding: '8px 20px', fontSize: '0.9rem' }}
                      disabled={loading}
                    >
                      {loading ? t('profile.bookingHistory.saving') : t('profile.bookingHistory.submitBtn')}
                    </button>
                    <button 
                      onClick={() => setShowReviewForm(null)}
                      style={{ background: 'transparent', border: '1px solid #cbd5e1', padding: '8px 20px', borderRadius: '6px', cursor: 'pointer', color: '#475569', fontWeight: '500' }}
                    >
                      {t('profile.bookingHistory.cancel')}
                    </button>
                  </div>
                </div>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default BookingHistoryTab;
