import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { FaMapMarkerAlt, FaStar, FaRegClock, FaCheckCircle, FaTimesCircle, FaUserFriends, FaShoppingCart, FaHeart, FaFire } from 'react-icons/fa';
import Swal from 'sweetalert2';
import { useQuery } from '@tanstack/react-query';
import { TourService } from '../services/TourService';
import { useCartStore } from '../store/useCartStore';
import { useAuthStore } from '../store/useAuthStore';
import TourCard from '../components/TourCard';
import api from '../api/axiosConfig';
import { formatPrice } from '../utils/formatPrice';
import '../styles/tourDetail.css';

const TourDetail: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  
  const { cart, addToCart } = useCartStore();
  const currentUser = useAuthStore(state => state.user);
  
  const [showAllReviews, setShowAllReviews] = useState(false);
  const [editingReviewId, setEditingReviewId] = useState<number | null>(null);
  const [editReviewData, setEditReviewData] = useState({ rating: 5, comment: '' });
  const [isSubmittingReview, setIsSubmittingReview] = useState(false);
  
  // Booking Form State
  const [guests, setGuests] = useState(1);
  const [startDate, setStartDate] = useState('');

  // 1. Fetch Tour Detail
  const { data: tour, isLoading: tourLoading } = useQuery({
    queryKey: ['tour', id],
    queryFn: () => TourService.getTourById(id!),
    enabled: !!id,
  });

  // 2. Fetch Reviews
  const { data: reviews = [] } = useQuery({
    queryKey: ['reviews', id],
    queryFn: () => TourService.getTourReviews(id!),
    enabled: !!id,
  });

  // 3. Fetch Related Tours
  const { data: relatedToursResponse } = useQuery({
    queryKey: ['relatedTours', tour?.destination],
    queryFn: () => TourService.getTours(0, 4, '', tour?.destination, '', '', undefined, 'id', 'ASC', [], [], 'ACTIVE,SOLD_OUT'),
    enabled: !!tour?.destination,
  });
  const relatedTours = (relatedToursResponse?.content || []).filter((t: any) => t.id.toString() !== id).slice(0, 3);

  // 4. Fetch Available Slots
  const { data: availableSlotsForDate = null } = useQuery({
    queryKey: ['tourSlots', id, startDate],
    queryFn: async () => {
      const res: any = await api.get(`/tours/${id}/schedules?date=${startDate}`);
      return res.data; // res is already the response body due to axios interceptor
    },
    enabled: !!(startDate && id),
    staleTime: 0, // Always fetch fresh slot data
  });

  // Gallery State
  const [showGallery, setShowGallery] = useState(false);
  const [currentImageIndex, setCurrentImageIndex] = useState(0);

  useEffect(() => {
    // Scroll to top when loading new tour
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }, [id]);

  const handleBooking = (e: React.FormEvent) => {
    e.preventDefault();
    if (isSoldOut) {
      Swal.fire({
        icon: 'warning',
        title: 'Tour Đã Hết Chỗ',
        text: 'Rất tiếc, tour này hiện tại đã hết chỗ. Vui lòng chọn ngày khởi hành khác hoặc tham khảo các tour tương tự.',
        confirmButtonColor: '#3b82f6'
      });
      return;
    }
    if (!startDate) {
      Swal.fire({
        icon: 'warning',
        title: 'Lỗi',
        text: 'Vui lòng chọn ngày khởi hành',
        confirmButtonColor: '#3b82f6'
      });
      return;
    }
    navigate(`/checkout/${id}`, { 
      state: { 
        tourId: tour.id, 
        guests, 
        startDate, 
        totalPrice: tour.price * guests,
        tourTitle: tour.title
      } 
    });
  };

  const handleAddToCart = () => {
    if (isSoldOut) {
      Swal.fire({
        icon: 'warning',
        title: 'Tour Đã Hết Chỗ',
        text: 'Rất tiếc, tour này hiện tại đã hết chỗ. Vui lòng chọn ngày khởi hành khác hoặc tham khảo các tour tương tự.',
        confirmButtonColor: '#3b82f6'
      });
      return;
    }
    if (!startDate) {
      Swal.fire({
        icon: 'warning',
        title: 'Chưa chọn ngày',
        text: 'Vui lòng chọn ngày khởi hành để thêm vào giỏ hàng',
        confirmButtonColor: '#3b82f6'
      });
      return;
    }
    
    addToCart({
      tourId: tour.id,
      tourTitle: tour.title,
      price: tour.price,
      imageUrl: tour.imageUrl || 'https://images.unsplash.com/photo-1528127269322-539801943592?auto=format&fit=crop&w=1000&q=80',
      guests: guests,
      startDate: startDate
    });
    
    const isExisting = cart.some(item => item.tourId === tour.id && item.startDate === startDate);
    const newCount = isExisting ? cart.length : cart.length + 1;

    Swal.fire({
      icon: 'success',
      title: 'Thành công!',
      html: `Đã thêm tour vào giỏ hàng thành công!<br><br><b>Giỏ hàng của bạn hiện đang có ${newCount} tour</b>`,
      confirmButtonColor: '#3b82f6'
    });
  };

  if (tourLoading) {
    return <div className="loading-spinner container" style={{minHeight: '60vh', paddingTop: '100px'}}>Đang tải dữ liệu...</div>;
  }

  if (!tour) {
    return <div className="container" style={{paddingTop: '100px'}}>Không tìm thấy tour.</div>;
  }

  const images = tour.images ? [...tour.images] : [];
  if (tour.imageUrl && !images.includes(tour.imageUrl)) {
    images.unshift(tour.imageUrl);
  }

  // Calculate dynamic rating based on fetched reviews
  const avgRating = reviews.length > 0 
    ? (reviews.reduce((acc: number, curr: any) => acc + curr.rating, 0) / reviews.length).toFixed(1) 
    : (tour.rating || 0).toFixed(1);
  const reviewCount = reviews.length > 0 ? reviews.length : (tour.reviewCount || 0);

  const scrollToReviews = () => {
    document.getElementById('reviews-section')?.scrollIntoView({ behavior: 'smooth', block: 'start' });
  };

  const slotsLeft = startDate 
    ? (availableSlotsForDate !== null ? availableSlotsForDate : (tour.availableSlots || 0))
    : (tour.availableSlots || 0);

  const isSoldOut = tour.status === 'SOLD_OUT' || slotsLeft <= 0;


  return (
    <div className="tour-detail-page">
      {/* Detail Header (Title & Meta) */}
      <div className="detail-header">
        <h1 className="detail-title">{tour.title}</h1>
        <div className="meta-badges">
          <div className="detail-rating">
            <FaStar className="star-icon" style={{color: '#f59e0b'}} />
            <span>{avgRating}</span>
            <span className="review-count" onClick={scrollToReviews}>({reviewCount} đánh giá)</span>
          </div>
          <span>•</span>
          <span className="badge"><FaMapMarkerAlt /> {tour.destination}</span>
          <span>•</span>
          <span className="badge"><FaRegClock /> {tour.duration}</span>
        </div>
      </div>

      {/* Masonry Photo Grid */}
      <div className="photo-grid-container">
        <div className="photo-grid">
          <div className="photo-grid-left">
            {images[0] ? <img src={images[0]} alt="Main Tour" className="photo-main" /> : <div className="photo-main" style={{background: '#e2e8f0', width: '100%', height: '100%', borderRadius: '16px'}}></div>}
          </div>
          <div className="photo-grid-right">
            {images[1] ? <img src={images[1]} alt="Tour 2" className="photo-small" /> : <div className="photo-small" style={{background: '#f1f5f9', borderRadius: '16px'}}></div>}
            {images[2] ? <img src={images[2]} alt="Tour 3" className="photo-small" /> : <div className="photo-small" style={{background: '#f1f5f9', borderRadius: '16px'}}></div>}
            {images[3] ? <img src={images[3]} alt="Tour 4" className="photo-small" /> : <div className="photo-small" style={{background: '#f1f5f9', borderRadius: '16px'}}></div>}
            {images[4] ? <img src={images[4]} alt="Tour 5" className="photo-small" /> : <div className="photo-small" style={{background: '#f1f5f9', borderRadius: '16px'}}></div>}
          </div>
        </div>
        <button className="view-all-btn" onClick={() => { setShowGallery(true); setCurrentImageIndex(0); }}>
          Hiển thị tất cả ảnh
        </button>
      </div>

      {/* Gallery Modal */}
      {showGallery && (
        <div className="gallery-modal" style={{ position: 'fixed', top: 0, left: 0, width: '100%', height: '100%', background: 'rgba(0,0,0,0.95)', zIndex: 9999, display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center' }}>
          <button onClick={() => setShowGallery(false)} style={{ position: 'absolute', top: '30px', right: '40px', background: 'none', border: 'none', color: 'white', fontSize: '2.5rem', cursor: 'pointer', opacity: 0.8, transition: 'opacity 0.2s' }} onMouseEnter={e => e.currentTarget.style.opacity='1'} onMouseLeave={e => e.currentTarget.style.opacity='0.8'}><FaTimesCircle /></button>
          
          <img src={images[currentImageIndex]} alt={`Gallery ${currentImageIndex + 1}`} style={{ maxWidth: '90%', maxHeight: '80vh', objectFit: 'contain', borderRadius: '8px', boxShadow: '0 10px 25px rgba(0,0,0,0.5)' }} />
          
          <div style={{ position: 'absolute', top: '50%', width: '100%', display: 'flex', justifyContent: 'space-between', padding: '0 5%', transform: 'translateY(-50%)' }}>
            <button onClick={() => setCurrentImageIndex(prev => prev > 0 ? prev - 1 : images.length - 1)} style={{ background: 'rgba(255,255,255,0.15)', border: 'none', color: 'white', fontSize: '2rem', width: '60px', height: '60px', borderRadius: '50%', cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center', backdropFilter: 'blur(5px)', transition: 'background 0.3s' }} onMouseEnter={e => e.currentTarget.style.background='rgba(255,255,255,0.3)'} onMouseLeave={e => e.currentTarget.style.background='rgba(255,255,255,0.15)'}>&#8249;</button>
            <button onClick={() => setCurrentImageIndex(prev => prev < images.length - 1 ? prev + 1 : 0)} style={{ background: 'rgba(255,255,255,0.15)', border: 'none', color: 'white', fontSize: '2rem', width: '60px', height: '60px', borderRadius: '50%', cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center', backdropFilter: 'blur(5px)', transition: 'background 0.3s' }} onMouseEnter={e => e.currentTarget.style.background='rgba(255,255,255,0.3)'} onMouseLeave={e => e.currentTarget.style.background='rgba(255,255,255,0.15)'}>&#8250;</button>
          </div>
          <div style={{ color: 'white', marginTop: '20px', fontSize: '1.2rem', letterSpacing: '2px', fontWeight: '500' }}>
            {currentImageIndex + 1} / {images.length}
          </div>
        </div>
      )}

      {/* Main Container */}
      <div className="container detail-container">
        <div className="detail-main">
          
          {/* Overview Section */}
          <section className="detail-section">
            <h2 className="section-heading">Tổng Quan Hành Trình</h2>
            <p className="detail-desc">{tour.description}</p>
          </section>

          {/* Inclusions & Exclusions */}
          <section className="detail-section">
            <h2 className="section-heading">Tour Này Có Gì?</h2>
            <div className="inclusions-grid">
              <div className="inclusion-list">
                <h3 style={{marginBottom: '16px'}}>✅ Bao gồm</h3>
                {tour.utilities && tour.utilities.filter((u: any) => u.isActive !== false).map((u: any, idx: number) => (
                  <div key={`u-${idx}`} className="inclusion-item yes"><FaCheckCircle /> {u.name}</div>
                ))}
                {tour.highlights && tour.highlights.map((h: string, idx: number) => (
                  <div key={`h-${idx}`} className="inclusion-item yes"><FaCheckCircle /> {h}</div>
                ))}
                {(!tour.utilities || tour.utilities.length === 0) && (!tour.highlights || tour.highlights.length === 0) && (
                  <div className="inclusion-item yes" style={{color: '#64748b'}}>Đang cập nhật...</div>
                )}
              </div>
              <div className="exclusion-list">
                <h3 style={{marginBottom: '16px'}}>❌ Không bao gồm</h3>
                <div className="inclusion-item no"><FaTimesCircle /> Thuế VAT 10%</div>
                <div className="inclusion-item no"><FaTimesCircle /> Chi phí cá nhân ngoài chương trình</div>
                <div className="inclusion-item no"><FaTimesCircle /> Tiền tip cho HDV và tài xế</div>
                <div className="inclusion-item no"><FaTimesCircle /> Phụ thu phòng đơn</div>
              </div>
            </div>
          </section>

          {/* Timeline Itinerary */}
          {tour.itinerary && tour.itinerary.length > 0 && (
            <section className="detail-section">
              <h2 className="section-heading">Lịch Trình Chi Tiết</h2>
              <div className="itinerary-timeline">
                {tour.itinerary.map((item: any, idx: number) => {
                  const dayText = String(item.day).toLowerCase().includes('ngày') ? item.day : `Ngày ${item.day}`;
                  return (
                    <div key={idx} className="timeline-item">
                      <div className="timeline-dot"></div>
                      <div className="timeline-content">
                        <div className="timeline-day">{dayText}: {item.title}</div>
                        <p className="timeline-desc">{item.description}</p>
                      </div>
                    </div>
                  );
                })}
              </div>
            </section>
          )}

          {/* Reviews from DB */}
          <section id="reviews-section" className="detail-section">
            <h2 className="section-heading">Đánh giá từ khách hàng</h2>
            
            {reviews.length > 0 ? (
              <>
                <div style={{display: 'flex', alignItems: 'center', gap: '16px', marginBottom: '24px'}}>
                  <FaStar style={{color: '#f59e0b', fontSize: '2rem'}} />
                  <span style={{fontSize: '2rem', fontWeight: 700}}>
                    {(reviews.reduce((acc: number, curr: any) => acc + curr.rating, 0) / reviews.length).toFixed(1)}
                  </span>
                  <span style={{color: '#64748b', fontSize: '1.1rem'}}>• {reviews.length} đánh giá</span>
                </div>
                <div style={{display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '24px'}}>
                  {(showAllReviews ? reviews : reviews.slice(0, 4)).map((review: any) => (
                    <div key={review.id} style={{padding: '16px', border: '1px solid #e2e8f0', borderRadius: '12px'}}>
                      <div style={{display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '12px'}}>
                        <div style={{width: '40px', height: '40px', borderRadius: '50%', background: '#cbd5e1', display: 'flex', justifyContent: 'center', alignItems: 'center', fontWeight: 'bold', color: 'white'}}>
                          {(review.fullName || review.username || 'U').charAt(0).toUpperCase()}
                        </div>
                        <div>
                          <h4 style={{margin: 0}}>{review.fullName || review.username || 'Khách hàng'}</h4>
                          <span style={{fontSize: '0.8rem', color: '#64748b'}}>
                            {new Date(review.createdAt || Date.now()).toLocaleDateString('vi-VN')}
                          </span>
                        </div>
                        <div style={{marginLeft: 'auto', color: '#f59e0b', display: 'flex', alignItems: 'center', gap: '10px'}}>
                          <span>{'★'.repeat(review.rating)}{'☆'.repeat(5 - review.rating)}</span>
                          {currentUser && currentUser.email === review.email && (
                            <button 
                              onClick={() => {
                                setEditingReviewId(review.id);
                                setEditReviewData({ rating: review.rating, comment: review.comment });
                              }}
                              style={{ background: 'transparent', border: 'none', color: '#2563eb', cursor: 'pointer', fontSize: '0.9rem', textDecoration: 'underline' }}
                            >
                              Chỉnh sửa
                            </button>
                          )}
                        </div>
                      </div>
                      
                      {editingReviewId === review.id ? (
                        <div style={{ marginTop: '10px' }}>
                          <div style={{ display: 'flex', gap: '5px', marginBottom: '10px' }}>
                            {[1, 2, 3, 4, 5].map(star => (
                              <FaStar 
                                key={star} 
                                onClick={() => setEditReviewData({...editReviewData, rating: star})}
                                style={{ cursor: 'pointer', color: star <= editReviewData.rating ? '#f59e0b' : '#cbd5e1', fontSize: '1.2rem' }} 
                              />
                            ))}
                          </div>
                          <textarea 
                            value={editReviewData.comment}
                            onChange={(e) => setEditReviewData({...editReviewData, comment: e.target.value})}
                            style={{ width: '100%', padding: '10px', borderRadius: '6px', border: '1px solid #cbd5e1', minHeight: '60px', marginBottom: '10px' }}
                          />
                          <div style={{ display: 'flex', gap: '10px' }}>
                            <button 
                              disabled={isSubmittingReview}
                              onClick={async () => {
                                try {
                                  setIsSubmittingReview(true);
                                  await api.put(`/reviews/${review.id}`, { tourId: tour.id, rating: editReviewData.rating, comment: editReviewData.comment });
                                  setEditingReviewId(null);
                                  // Can't use setReviews with React Query easily unless we update cache. Simplest is to reload page or we should use refetch.
                                  // Wait, since I removed refetchReviews, I should just do window.location.reload() or re-fetch.
                                  // For simplicity:
                                  window.location.reload();
                                  Swal.fire({ icon: 'success', title: 'Cập nhật thành công', showConfirmButton: false, timer: 1500 });
                                } catch (e) {
                                  Swal.fire({ icon: 'error', title: 'Lỗi', text: 'Cập nhật thất bại' });
                                } finally {
                                  setIsSubmittingReview(false);
                                }
                              }}
                              style={{ background: '#2563eb', color: '#fff', border: 'none', padding: '6px 12px', borderRadius: '6px', cursor: 'pointer', fontSize: '0.85rem' }}
                            >
                              {isSubmittingReview ? 'Đang lưu...' : 'Lưu lại'}
                            </button>
                            <button 
                              onClick={() => setEditingReviewId(null)}
                              style={{ background: 'transparent', border: '1px solid #cbd5e1', padding: '6px 12px', borderRadius: '6px', cursor: 'pointer', fontSize: '0.85rem' }}
                            >
                              Hủy
                            </button>
                          </div>
                        </div>
                      ) : (
                        <p style={{color: '#475569', fontSize: '0.95rem'}}>{review.comment}</p>
                      )}
                    </div>
                  ))}
                </div>
                {reviews.length > 4 && (
                  <div style={{ textAlign: 'center', marginTop: '20px' }}>
                    <button 
                      onClick={() => setShowAllReviews(!showAllReviews)}
                      style={{ background: 'transparent', border: '1px solid #2563eb', color: '#2563eb', padding: '8px 24px', borderRadius: '24px', cursor: 'pointer', fontWeight: 'bold', transition: 'all 0.2s' }}
                      onMouseEnter={(e) => { e.currentTarget.style.background = '#2563eb'; e.currentTarget.style.color = '#fff'; }}
                      onMouseLeave={(e) => { e.currentTarget.style.background = 'transparent'; e.currentTarget.style.color = '#2563eb'; }}
                    >
                      {showAllReviews ? 'Thu gọn' : `Xem tất cả ${reviews.length} đánh giá`}
                    </button>
                  </div>
                )}
              </>
            ) : (
              <p style={{color: '#64748b'}}>Chưa có đánh giá nào cho tour này. Hãy là người đầu tiên trải nghiệm và đánh giá!</p>
            )}
          </section>
        </div>

        {/* Sticky Booking Sidebar */}
        <div className="detail-sidebar">
          <div className="booking-card">
            
            <div className="fomo-badge" style={isSoldOut ? {background: '#fee2e2', color: '#ef4444'} : {}}>
              {startDate ? (
                isSoldOut ? (
                  <><FaTimesCircle /> Rất tiếc, chuyến này đã hết chỗ!</>
                ) : (
                  <><FaFire /> Đang bán chạy! Chuyến này còn {slotsLeft} chỗ trống</>
                )
              ) : (
                isSoldOut ? (
                  <><FaTimesCircle /> Rất tiếc, tour này tạm thời hết chỗ!</>
                ) : (
                  <><FaFire /> Đang bán chạy! Hiện còn {slotsLeft} chỗ. Chọn ngày để xem số chỗ chính xác</>
                )
              )}
            </div>

            <div style={{ position: 'absolute', top: '24px', right: '24px', cursor: 'pointer' }}>
              <FaHeart style={{ color: '#cbd5e1', fontSize: '1.5rem', transition: 'color 0.2s' }} 
                onMouseEnter={(e) => e.currentTarget.style.color = '#ef4444'} 
                onMouseLeave={(e) => e.currentTarget.style.color = '#cbd5e1'} 
              />
            </div>

            <div className="price-header">
              {formatPrice(tour.price)} <span>/ người</span>
            </div>
            
            <form onSubmit={handleBooking} className="booking-form">
              <div className="form-group">
                <label>Ngày khởi hành</label>
                <input 
                  type="date" 
                  value={startDate}
                  onChange={(e) => setStartDate(e.target.value)}
                  min={new Date().toISOString().split('T')[0]}
                  required
                />
              </div>
              
              <div className="form-group">
                <label>Số lượng hành khách</label>
                <div style={{position: 'relative'}}>
                  <FaUserFriends style={{position: 'absolute', left: '12px', top: '50%', transform: 'translateY(-50%)', color: '#64748b'}} />
                  <input 
                    type="number" 
                    min="1" 
                    max={slotsLeft > 0 ? slotsLeft : 1}
                    value={guests}
                    onChange={(e) => setGuests(parseInt(e.target.value))}
                    required
                    disabled={isSoldOut}
                    style={{paddingLeft: '36px', width: '100%', ...(isSoldOut ? {backgroundColor: '#f1f5f9', cursor: 'not-allowed'} : {})}}
                  />
                </div>
              </div>

              <div className="price-breakdown">
                <div className="breakdown-row">
                  <span>Giá ({guests} người)</span>
                  <span>{formatPrice(tour.price * guests)}</span>
                </div>
                <div className="breakdown-row">
                  <span>Thuế & Phí dịch vụ</span>
                  <span>Miễn phí</span>
                </div>
                <div className="breakdown-row total">
                  <span>Tổng tiền</span>
                  <span>{formatPrice(tour.price * guests)}</span>
                </div>
              </div>
              
              <button type="submit" className="btn-book" style={isSoldOut ? {opacity: 0.7, cursor: 'pointer', background: '#94a3b8'} : {}}>
                {isSoldOut ? 'Đã Hết Chỗ' : 'Đặt Tour Ngay'}
              </button>
              <button type="button" onClick={handleAddToCart} className="btn-cart" style={isSoldOut ? {opacity: 0.7, cursor: 'pointer', border: '1px solid #94a3b8', color: '#94a3b8'} : {}}>
                <FaShoppingCart /> Thêm Vào Giỏ Hàng
              </button>
            </form>
          </div>
        </div>
      </div>

      {/* Related Tours Section */}
      {relatedTours.length > 0 && (
        <div className="related-tours">
          <h2 className="section-heading" style={{textAlign: 'center', marginBottom: '40px'}}>Có Thể Bạn Cũng Thích</h2>
          <div style={{display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))', gap: '30px'}}>
            {relatedTours.map((t: any) => (
              <TourCard key={t.id} tour={t} />
            ))}
          </div>
        </div>
      )}
    </div>
  );
};

export default TourDetail;
