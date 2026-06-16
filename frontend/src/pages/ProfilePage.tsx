import React, { useState, useEffect } from 'react';
import { FaUser, FaLock, FaHistory, FaCheckCircle, FaTimesCircle, FaEye, FaEyeSlash, FaSignOutAlt, FaFileInvoiceDollar, FaStar, FaDownload, FaMapMarkerAlt, FaCalendarAlt, FaUsers, FaIdCard, FaEnvelope, FaPhone, FaStickyNote, FaMoneyBillWave, FaMap } from 'react-icons/fa';
import api from '../api/axiosConfig';
import { useAuthStore } from '../store/useAuthStore';
import { UserService } from '../services/UserService';
import { BookingService } from '../services/BookingService';
import { useNavigate } from 'react-router-dom';
import html2canvas from 'html2canvas';
import jsPDF from 'jspdf';
import TicketTemplate from '../components/TicketTemplate';
import { useRef } from 'react';
import { formatPrice } from '../utils/formatPrice';
import '../styles/tourDetail.css';

const ProfilePage: React.FC = () => {
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState('profile');
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState({ text: '', type: '' });
  
  // Profile State
  const [profile, setProfile] = useState({ fullName: '', email: '', phone: '', avatar: '' });
  const [uploadingAvatar, setUploadingAvatar] = useState(false);
  
  // Password State
  const [passwords, setPasswords] = useState({ oldPassword: '', newPassword: '', confirmPassword: '' });
  const [showOldPass, setShowOldPass] = useState(false);
  const [showNewPass, setShowNewPass] = useState(false);
  const [showConfirmPass, setShowConfirmPass] = useState(false);

  // Bookings State
  const [bookings, setBookings] = useState<any[]>([]);
  const [selectedBooking, setSelectedBooking] = useState<any>(null);

  // Review State
  const [showReviewForm, setShowReviewForm] = useState<number | null>(null);
  const [reviewData, setReviewData] = useState({ rating: 5, comment: '' });

  // Print State
  const ticketRef = useRef<HTMLDivElement>(null);
  const [printingBooking, setPrintingBooking] = useState<any>(null);
  const [isGeneratingPDF, setIsGeneratingPDF] = useState(false);

  const triggerPrint = (booking: any) => {
    setPrintingBooking(booking);
    setIsGeneratingPDF(true);
    
    // Đợi DOM render xong booking mới
    setTimeout(async () => {
      if (ticketRef.current) {
        try {
          const canvas = await html2canvas(ticketRef.current, { scale: 2 });
          const imgData = canvas.toDataURL('image/png');
          
          const pdf = new jsPDF({
            orientation: 'portrait',
            unit: 'mm',
            format: 'a4'
          });
          
          const pdfWidth = pdf.internal.pageSize.getWidth();
          const pdfHeight = (canvas.height * pdfWidth) / canvas.width;
          
          pdf.addImage(imgData, 'PNG', 0, 0, pdfWidth, pdfHeight);
          pdf.save(`Ve_E_Ticket_${booking.id}.pdf`);
        } catch (error) {
          console.error("Lỗi khi tạo PDF:", error);
          alert("Có lỗi xảy ra khi tạo vé. Vui lòng thử lại!");
        } finally {
          setIsGeneratingPDF(false);
        }
      }
    }, 500);
  };

  useEffect(() => {
    const fetchInitialData = async () => {
      try {
        const userProfile = await UserService.getMyProfile();
        setProfile({
          fullName: userProfile.fullName || '',
          email: userProfile.email || '',
          phone: userProfile.phone || '',
          avatar: userProfile.avatar || ''
        });

        const myBookings = await BookingService.getMyBookings();
        if (myBookings && Array.isArray(myBookings)) {
          setBookings(myBookings);
        }
      } catch (error) {
        console.error('Failed to fetch user data', error);
      }
    };
    fetchInitialData();
  }, []);

  const handleAvatarUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    if (!e.target.files || e.target.files.length === 0) return;
    const file = e.target.files[0];
    const formData = new FormData();
    formData.append('file', file);
    setUploadingAvatar(true);
    try {
      const res = await api.post('/upload', formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      });
      const url = res.data?.data?.url || res.data?.url || res.data;
      if (typeof url === 'string') {
        await UserService.updateAvatar(url);
        setProfile({ ...profile, avatar: url });
        setMessage({ text: 'Cập nhật ảnh đại diện thành công!', type: 'success' });
        const user = useAuthStore.getState().user;
        if (user) {
          useAuthStore.setState({ user: { ...user, avatar: url } });
        }
      }
    } catch (err: any) {
      const errorMessage = err.response?.data?.message || 'Lỗi tải ảnh lên!';
      setMessage({ text: errorMessage, type: 'error' });
    } finally {
      setUploadingAvatar(false);
    }
  };

  const handleProfileSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    try {
      await UserService.updateProfile(profile);
      setMessage({ text: 'Cập nhật hồ sơ thành công!', type: 'success' });
    } catch (error: any) {
      setMessage({ text: error.response?.data?.message || 'Cập nhật thất bại!', type: 'error' });
    } finally {
      setLoading(false);
    }
  };

  const handlePasswordSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (passwords.newPassword.length < 8) {
      setMessage({ text: 'Mật khẩu mới phải có ít nhất 8 ký tự!', type: 'error' });
      return;
    }
    if (passwords.newPassword !== passwords.confirmPassword) {
      setMessage({ text: 'Mật khẩu xác nhận không khớp!', type: 'error' });
      return;
    }
    setLoading(true);
    try {
      await UserService.changePassword(passwords);
      setMessage({ text: 'Đổi mật khẩu thành công!', type: 'success' });
      setPasswords({ oldPassword: '', newPassword: '', confirmPassword: '' });
    } catch (error: any) {
      setMessage({ text: error.response?.data?.message || 'Đổi mật khẩu thất bại!', type: 'error' });
    } finally {
      setLoading(false);
    }
  };

  const renderMessage = () => {
    if (!message.text) return null;
    return (
      <div style={{ padding: '10px', marginBottom: '20px', borderRadius: '8px', background: message.type === 'success' ? '#dcfce7' : '#fee2e2', color: message.type === 'success' ? '#166534' : '#991b1b', display: 'flex', alignItems: 'center', gap: '8px' }}>
        {message.type === 'success' ? <FaCheckCircle /> : <FaTimesCircle />}
        {message.text}
      </div>
    );
  };

  const handleLogout = () => {
    useAuthStore.getState().logout();
    navigate('/login');
  };

  return (
    <div className="container" style={{ paddingTop: '100px', paddingBottom: '50px', minHeight: 'calc(100vh - 100px)' }}>
      <div style={{ display: 'flex', gap: '30px', flexWrap: 'wrap' }}>
        
        {/* Sidebar */}
        <div style={{ width: '250px', flexShrink: 0, background: '#fff', padding: '20px', borderRadius: '12px', boxShadow: '0 4px 6px rgba(0,0,0,0.05)', height: 'fit-content' }}>
          <h2 style={{ fontSize: '1.2rem', marginBottom: '20px', color: '#0f172a' }}>Tài Khoản</h2>
          <ul style={{ listStyle: 'none', padding: 0, margin: 0 }}>
            <li 
              onClick={() => { setActiveTab('profile'); setMessage({text:'', type:''}); }}
              style={{ padding: '12px 15px', cursor: 'pointer', borderRadius: '8px', marginBottom: '5px', background: activeTab === 'profile' ? '#f1f5f9' : 'transparent', color: activeTab === 'profile' ? '#2563eb' : '#475569', fontWeight: activeTab === 'profile' ? '600' : '400', display: 'flex', alignItems: 'center', gap: '10px' }}
            >
              <FaUser /> Thông tin hồ sơ
            </li>
            <li 
              onClick={() => { setActiveTab('bookings'); setMessage({text:'', type:''}); }}
              style={{ padding: '12px 15px', cursor: 'pointer', borderRadius: '8px', marginBottom: '5px', background: activeTab === 'bookings' ? '#f1f5f9' : 'transparent', color: activeTab === 'bookings' ? '#2563eb' : '#475569', fontWeight: activeTab === 'bookings' ? '600' : '400', display: 'flex', alignItems: 'center', gap: '10px' }}
            >
              <FaHistory /> Lịch sử đặt tour
            </li>
            <li 
              onClick={() => { setActiveTab('password'); setMessage({text:'', type:''}); }}
              style={{ padding: '12px 15px', cursor: 'pointer', borderRadius: '8px', marginBottom: '5px', background: activeTab === 'password' ? '#f1f5f9' : 'transparent', color: activeTab === 'password' ? '#2563eb' : '#475569', fontWeight: activeTab === 'password' ? '600' : '400', display: 'flex', alignItems: 'center', gap: '10px' }}
            >
              <FaLock /> Đổi mật khẩu
            </li>
            <li 
              onClick={() => { setActiveTab('billing'); setMessage({text:'', type:''}); }}
              style={{ padding: '12px 15px', cursor: 'pointer', borderRadius: '8px', marginBottom: '5px', background: activeTab === 'billing' ? '#f1f5f9' : 'transparent', color: activeTab === 'billing' ? '#2563eb' : '#475569', fontWeight: activeTab === 'billing' ? '600' : '400', display: 'flex', alignItems: 'center', gap: '10px' }}
            >
              <FaFileInvoiceDollar /> Hóa đơn của tôi
            </li>
            <li 
              onClick={handleLogout}
              style={{ padding: '12px 15px', cursor: 'pointer', borderRadius: '8px', background: 'transparent', color: '#e11d48', fontWeight: '400', display: 'flex', alignItems: 'center', gap: '10px', marginTop: '10px', borderTop: '1px solid #e2e8f0' }}
            >
              <FaSignOutAlt /> Đăng xuất
            </li>
          </ul>
        </div>

        {/* Content Area */}
        <div style={{ flex: 1, minWidth: '300px', background: '#fff', padding: '30px', borderRadius: '12px', boxShadow: '0 4px 6px rgba(0,0,0,0.05)' }}>
          {renderMessage()}

          {activeTab === 'profile' && (
            <div>
              <h2 style={{ marginBottom: '20px', color: '#0f172a' }}>Hồ Sơ Của Bạn</h2>
              <div style={{ display: 'flex', alignItems: 'center', gap: '20px', marginBottom: '20px' }}>
                <div style={{ width: '100px', height: '100px', borderRadius: '50%', background: '#f1f5f9', overflow: 'hidden', display: 'flex', justifyContent: 'center', alignItems: 'center', position: 'relative' }}>
                  {profile.avatar ? (
                    <img src={profile.avatar} alt="Avatar" style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
                  ) : (
                    <FaUser style={{ fontSize: '3rem', color: '#cbd5e1' }} />
                  )}
                </div>
                <div>
                  <input type="file" id="avatar-upload" style={{ display: 'none' }} accept="image/*" onChange={handleAvatarUpload} />
                  <label htmlFor="avatar-upload" style={{ cursor: 'pointer', display: 'inline-block', padding: '8px 16px', background: '#f1f5f9', color: '#334155', borderRadius: '6px', fontWeight: 500, border: '1px solid #e2e8f0', transition: 'all 0.2s' }}>
                    {uploadingAvatar ? 'Đang tải...' : 'Thay đổi ảnh'}
                  </label>
                  <p style={{ margin: '5px 0 0', fontSize: '0.8rem', color: '#94a3b8' }}>Chấp nhận JPG, PNG hoặc GIF</p>
                </div>
              </div>
              <form onSubmit={handleProfileSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '15px' }}>
                <div className="input-group">
                  <label className="input-label">Họ và tên</label>
                  <input type="text" className="input-field" value={profile.fullName} onChange={e => setProfile({...profile, fullName: e.target.value})} required />
                </div>
                <div className="input-group">
                  <label className="input-label">Email</label>
                  <input type="email" className="input-field" value={profile.email} onChange={e => setProfile({...profile, email: e.target.value})} required />
                </div>
                <div className="input-group">
                  <label className="input-label">Số điện thoại</label>
                  <input type="text" className="input-field" value={profile.phone} onChange={e => setProfile({...profile, phone: e.target.value})} required />
                </div>
                <button type="submit" className="btn btn-primary" disabled={loading} style={{ alignSelf: 'flex-start', marginTop: '10px' }}>
                  {loading ? 'Đang cập nhật...' : 'Cập Nhật Hồ Sơ'}
                </button>
              </form>
            </div>
          )}

          {activeTab === 'password' && (
            <div>
              <h2 style={{ marginBottom: '5px', color: '#0f172a' }}>Đổi Mật Khẩu</h2>
              <p style={{ color: '#64748b', fontSize: '0.9rem', marginBottom: '20px' }}>
                Mật khẩu mới nên có ít nhất 8 ký tự, gồm chữ hoa, chữ thường và số.
              </p>
              <form onSubmit={handlePasswordSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '15px', maxWidth: '400px' }}>
                <div className="input-group">
                  <label className="input-label">Mật khẩu hiện tại</label>
                  <div style={{ position: 'relative' }}>
                    <input type={showOldPass ? "text" : "password"} className="input-field" value={passwords.oldPassword} onChange={e => setPasswords({...passwords, oldPassword: e.target.value})} required style={{ paddingRight: '40px', width: '100%' }} />
                    <button type="button" onClick={() => setShowOldPass(!showOldPass)} style={{ position: 'absolute', right: '10px', top: '50%', transform: 'translateY(-50%)', background: 'none', border: 'none', color: '#64748b', cursor: 'pointer' }}>
                      {showOldPass ? <FaEyeSlash /> : <FaEye />}
                    </button>
                  </div>
                </div>
                <div className="input-group">
                  <label className="input-label">Mật khẩu mới</label>
                  <div style={{ position: 'relative' }}>
                    <input type={showNewPass ? "text" : "password"} className="input-field" value={passwords.newPassword} onChange={e => setPasswords({...passwords, newPassword: e.target.value})} required style={{ paddingRight: '40px', width: '100%' }} />
                    <button type="button" onClick={() => setShowNewPass(!showNewPass)} style={{ position: 'absolute', right: '10px', top: '50%', transform: 'translateY(-50%)', background: 'none', border: 'none', color: '#64748b', cursor: 'pointer' }}>
                      {showNewPass ? <FaEyeSlash /> : <FaEye />}
                    </button>
                  </div>
                </div>
                <div className="input-group">
                  <label className="input-label">Xác nhận mật khẩu mới</label>
                  <div style={{ position: 'relative' }}>
                    <input type={showConfirmPass ? "text" : "password"} className="input-field" value={passwords.confirmPassword} onChange={e => setPasswords({...passwords, confirmPassword: e.target.value})} required style={{ paddingRight: '40px', width: '100%' }} />
                    <button type="button" onClick={() => setShowConfirmPass(!showConfirmPass)} style={{ position: 'absolute', right: '10px', top: '50%', transform: 'translateY(-50%)', background: 'none', border: 'none', color: '#64748b', cursor: 'pointer' }}>
                      {showConfirmPass ? <FaEyeSlash /> : <FaEye />}
                    </button>
                  </div>
                </div>
                <button type="submit" className="btn btn-primary" disabled={loading} style={{ alignSelf: 'flex-start', marginTop: '10px' }}>
                  {loading ? 'Đang đổi mật khẩu...' : 'Cập nhật mật khẩu'}
                </button>
              </form>
            </div>
          )}

          {activeTab === 'billing' && (
            <div>
              <h2 style={{ marginBottom: '20px', color: '#0f172a' }}>Hóa Đơn Của Tôi</h2>
              {bookings.filter(b => b.status === 'PAID').length === 0 ? (
                <div style={{ padding: '40px', textAlign: 'center', background: '#f8fafc', borderRadius: '12px', border: '1px dashed #cbd5e1' }}>
                  <FaFileInvoiceDollar style={{ fontSize: '3rem', color: '#94a3b8', marginBottom: '15px' }} />
                  <h3 style={{ color: '#475569', marginBottom: '10px' }}>Chưa có hóa đơn nào</h3>
                  <p style={{ color: '#64748b' }}>Các hóa đơn và biên lai thanh toán của bạn sẽ hiển thị tại đây.</p>
                </div>
              ) : (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '15px' }}>
                  {bookings.filter(b => b.status === 'PAID').map((booking, idx) => (
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
          )}

          {activeTab === 'bookings' && (
            <div>
              <h2 style={{ marginBottom: '20px', color: '#0f172a' }}>Lịch Sử Đặt Tour</h2>
              {bookings.length === 0 ? (
                <p style={{ color: '#64748b' }}>Bạn chưa đặt tour nào.</p>
              ) : (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '15px' }}>
                  {bookings.map((booking, idx) => (
                    <div key={idx} style={{ padding: '20px', border: '1px solid #e2e8f0', borderRadius: '8px', display: 'flex', flexWrap: 'wrap', gap: '20px', justifyContent: 'space-between', alignItems: 'center' }}>
                      <div>
                        <h3 style={{ margin: '0 0 5px 0', fontSize: '1.1rem', color: '#1e293b' }}>Mã Đơn: #{booking.id}</h3>
                        <p style={{ margin: '0 0 5px 0', color: '#475569' }}>Ngày đặt: {new Date(booking.bookingDate).toLocaleDateString('vi-VN')}</p>
                        <p style={{ margin: '0 0 5px 0', color: '#475569' }}>Số lượng: {booking.numberOfPeople} người</p>
                        <p style={{ margin: '0', color: '#e11d48', fontWeight: 'bold' }}>Tổng tiền: {formatPrice(booking.totalPrice)}</p>
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
                          {booking.status === 'PENDING' ? 'Chờ Duyệt' : booking.status === 'CONFIRMED' ? 'Chưa Thanh Toán' : booking.status === 'PAID' ? 'Đã Thanh Toán' : 'Đã Hủy'}
                        </span>
                        <button 
                          onClick={() => setSelectedBooking(booking)}
                          style={{ background: 'transparent', border: '1px solid #cbd5e1', padding: '6px 12px', borderRadius: '6px', fontSize: '0.85rem', cursor: 'pointer', color: '#475569', transition: 'all 0.2s' }}
                          onMouseEnter={(e) => { e.currentTarget.style.background = '#f1f5f9'; e.currentTarget.style.borderColor = '#94a3b8'; }}
                          onMouseLeave={(e) => { e.currentTarget.style.background = 'transparent'; e.currentTarget.style.borderColor = '#cbd5e1'; }}
                        >
                          Xem chi tiết
                        </button>
                        {booking.status === 'CONFIRMED' && (
                          <button 
                            onClick={async () => {
                              try {
                                const res = await BookingService.createPayOSUrl(booking.id);
                                const paymentData = res?.data || res;
                                const url = typeof paymentData === 'string' ? paymentData : paymentData?.paymentUrl;
                                if (url) {
                                  window.location.href = url;
                                } else {
                                  alert('Không tìm thấy đường dẫn thanh toán từ máy chủ');
                                }
                              } catch(e: any) {
                                console.error('Lỗi tạo link thanh toán:', e);
                                const errorMsg = e.response?.data?.message || 'Lỗi tạo link thanh toán';
                                alert(errorMsg);
                              }
                            }}
                            style={{ background: '#10b981', border: 'none', padding: '6px 12px', borderRadius: '6px', color: '#fff', cursor: 'pointer', fontWeight: 'bold' }}
                          >
                            Thanh Toán Ngay
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
                              <FaDownload /> {isGeneratingPDF ? 'Đang tạo...' : 'Tải Vé PDF'}
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
                              {booking.reviewed ? 'Chỉnh sửa đánh giá' : 'Đánh giá tour'}
                            </button>
                          </div>
                        )}
                      </div>
                      
                      {/* Review Inline Form */}
                      {showReviewForm === booking.id && (
                        <div style={{ width: '100%', marginTop: '15px', padding: '20px', background: '#f8fafc', borderRadius: '8px', border: '1px solid #e2e8f0', animation: 'fadeIn 0.3s ease-in-out' }}>
                          <h4 style={{ margin: '0 0 10px 0', color: '#0f172a' }}>Đánh giá tour: {booking.tourTitle}</h4>
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
                            placeholder="Chia sẻ trải nghiệm của bạn về tour này..." 
                            value={reviewData.comment}
                            onChange={e => setReviewData({...reviewData, comment: e.target.value})}
                            style={{ width: '100%', padding: '12px', borderRadius: '8px', border: '1px solid #cbd5e1', minHeight: '100px', marginBottom: '15px', resize: 'vertical', fontFamily: 'inherit' }}
                          />
                          <div style={{ display: 'flex', gap: '10px' }}>
                            <button 
                              onClick={async () => {
                                try {
                                  setLoading(true);
                                  if (booking.reviewed) {
                                    await api.put(`/reviews/${booking.reviewId}`, { tourId: booking.tourId, rating: reviewData.rating, comment: reviewData.comment });
                                    setMessage({ text: 'Cập nhật đánh giá thành công!', type: 'success' });
                                    booking.reviewRating = reviewData.rating;
                                    booking.reviewComment = reviewData.comment;
                                  } else {
                                    const res = await api.post('/reviews', { tourId: booking.tourId, rating: reviewData.rating, comment: reviewData.comment });
                                    setMessage({ text: 'Cảm ơn bạn đã đánh giá!', type: 'success' });
                                    booking.reviewed = true;
                                    booking.reviewId = res.data.data.id;
                                    booking.reviewRating = reviewData.rating;
                                    booking.reviewComment = reviewData.comment;
                                  }
                                  setShowReviewForm(null);
                                } catch(e: any) {
                                  setMessage({ text: e.response?.data?.message || 'Gửi đánh giá thất bại!', type: 'error' });
                                } finally {
                                  setLoading(false);
                                  window.scrollTo({ top: 0, behavior: 'smooth' });
                                }
                              }}
                              className="btn btn-primary" 
                              style={{ padding: '8px 20px', fontSize: '0.9rem' }}
                              disabled={loading}
                            >
                              {loading ? 'Đang gửi...' : (booking.reviewed ? 'Lưu Thay Đổi' : 'Gửi Đánh Giá')}
                            </button>
                            <button 
                              onClick={() => setShowReviewForm(null)}
                              style={{ background: 'transparent', border: '1px solid #cbd5e1', padding: '8px 20px', borderRadius: '6px', cursor: 'pointer', color: '#475569', fontWeight: '500' }}
                            >
                              Hủy
                            </button>
                          </div>
                        </div>
                      )}
                    </div>
                  ))}
                </div>
              )}
            </div>
          )}

        </div>
      </div>

      {/* Booking Details Modal */}
      {selectedBooking && (
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
                  <h2 style={{ color: '#fff', margin: 0, fontSize: '1.5rem', fontWeight: '700' }}>Chi Tiết Đơn Hàng #{selectedBooking.id}</h2>
                  <p style={{ color: '#94a3b8', margin: '5px 0 0 0', fontSize: '0.9rem' }}>Đặt lúc: {new Date(selectedBooking.bookingDate).toLocaleString('vi-VN')}</p>
                </div>
              </div>
            </div>
            
            {/* Body */}
            <div style={{ padding: '30px', overflowY: 'auto' }}>
              
              {/* Tour Info Card */}
              <div style={{ background: '#fff', borderRadius: '12px', padding: '20px', boxShadow: '0 4px 6px -1px rgba(0,0,0,0.05), 0 2px 4px -1px rgba(0,0,0,0.03)', marginBottom: '20px', border: '1px solid #e2e8f0' }}>
                <h3 style={{ margin: '0 0 15px 0', color: '#0f172a', fontSize: '1.1rem', display: 'flex', alignItems: 'center', gap: '8px', borderBottom: '1px solid #f1f5f9', paddingBottom: '10px' }}>
                  <FaMap color="#3b82f6" /> Thông tin chuyến đi
                </h3>
                
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '15px' }}>
                  <div style={{ display: 'flex', alignItems: 'flex-start', gap: '10px' }}>
                    <div style={{ color: '#64748b', marginTop: '3px' }}><FaMapMarkerAlt /></div>
                    <div>
                      <div style={{ fontSize: '0.85rem', color: '#64748b', marginBottom: '2px' }}>Tên Tour</div>
                      <div style={{ color: '#0f172a', fontWeight: '600' }}>{selectedBooking.tourTitle || 'Đang cập nhật'}</div>
                    </div>
                  </div>
                  <div style={{ display: 'flex', alignItems: 'flex-start', gap: '10px' }}>
                    <div style={{ color: '#64748b', marginTop: '3px' }}><FaMapMarkerAlt /></div>
                    <div>
                      <div style={{ fontSize: '0.85rem', color: '#64748b', marginBottom: '2px' }}>Địa điểm</div>
                      <div style={{ color: '#0f172a', fontWeight: '500' }}>{selectedBooking.destination || 'Đang cập nhật'}</div>
                    </div>
                  </div>
                  <div style={{ display: 'flex', alignItems: 'flex-start', gap: '10px' }}>
                    <div style={{ color: '#64748b', marginTop: '3px' }}><FaCalendarAlt /></div>
                    <div>
                      <div style={{ fontSize: '0.85rem', color: '#64748b', marginBottom: '2px' }}>Ngày đi</div>
                      <div style={{ color: '#0f172a', fontWeight: '500' }}>{selectedBooking.travelDate ? new Date(selectedBooking.travelDate).toLocaleDateString('vi-VN') : 'N/A'}</div>
                    </div>
                  </div>
                  <div style={{ display: 'flex', alignItems: 'flex-start', gap: '10px' }}>
                    <div style={{ color: '#64748b', marginTop: '3px' }}><FaUsers /></div>
                    <div>
                      <div style={{ fontSize: '0.85rem', color: '#64748b', marginBottom: '2px' }}>Số hành khách</div>
                      <div style={{ color: '#0f172a', fontWeight: '500' }}>{selectedBooking.numberOfPeople} người</div>
                    </div>
                  </div>
                </div>
              </div>

              {/* Customer Info Card */}
              <div style={{ background: '#fff', borderRadius: '12px', padding: '20px', boxShadow: '0 4px 6px -1px rgba(0,0,0,0.05), 0 2px 4px -1px rgba(0,0,0,0.03)', marginBottom: '20px', border: '1px solid #e2e8f0' }}>
                <h3 style={{ margin: '0 0 15px 0', color: '#0f172a', fontSize: '1.1rem', display: 'flex', alignItems: 'center', gap: '8px', borderBottom: '1px solid #f1f5f9', paddingBottom: '10px' }}>
                  <FaIdCard color="#8b5cf6" /> Thông tin người đặt
                </h3>
                
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '15px' }}>
                  <div style={{ display: 'flex', alignItems: 'flex-start', gap: '10px' }}>
                    <div style={{ color: '#64748b', marginTop: '3px' }}><FaUser /></div>
                    <div>
                      <div style={{ fontSize: '0.85rem', color: '#64748b', marginBottom: '2px' }}>Họ và tên</div>
                      <div style={{ color: '#0f172a', fontWeight: '500' }}>{selectedBooking.customerName || profile.fullName}</div>
                    </div>
                  </div>
                  <div style={{ display: 'flex', alignItems: 'flex-start', gap: '10px' }}>
                    <div style={{ color: '#64748b', marginTop: '3px' }}><FaPhone /></div>
                    <div>
                      <div style={{ fontSize: '0.85rem', color: '#64748b', marginBottom: '2px' }}>Số điện thoại</div>
                      <div style={{ color: '#0f172a', fontWeight: '500' }}>{selectedBooking.customerPhone || profile.phone || 'Không cung cấp'}</div>
                    </div>
                  </div>
                  <div style={{ display: 'flex', alignItems: 'flex-start', gap: '10px', gridColumn: '1 / -1' }}>
                    <div style={{ color: '#64748b', marginTop: '3px' }}><FaEnvelope /></div>
                    <div>
                      <div style={{ fontSize: '0.85rem', color: '#64748b', marginBottom: '2px' }}>Email</div>
                      <div style={{ color: '#0f172a', fontWeight: '500' }}>{selectedBooking.customerEmail || profile.email || 'Không cung cấp'}</div>
                    </div>
                  </div>
                  {selectedBooking.note && (
                    <div style={{ display: 'flex', alignItems: 'flex-start', gap: '10px', gridColumn: '1 / -1', background: '#f8fafc', padding: '10px', borderRadius: '8px' }}>
                      <div style={{ color: '#64748b', marginTop: '3px' }}><FaStickyNote /></div>
                      <div>
                        <div style={{ fontSize: '0.85rem', color: '#64748b', marginBottom: '2px' }}>Ghi chú</div>
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
                    <FaCheckCircle /> Trạng thái đơn hàng:
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
                    {selectedBooking.status}
                  </span>
                </div>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderTop: '1px dashed #93c5fd', paddingTop: '15px' }}>
                  <span style={{ color: '#1e3a8a', fontWeight: '700', fontSize: '1.2rem', display: 'flex', alignItems: 'center', gap: '8px' }}>
                    <FaMoneyBillWave /> Tổng thanh toán:
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
                  Xem Tour
                </button>
              )}
              <button 
                onClick={() => setSelectedBooking(null)} 
                style={{ background: '#3b82f6', color: '#fff', border: 'none', padding: '10px 25px', borderRadius: '8px', fontWeight: '600', cursor: 'pointer', boxShadow: '0 4px 6px -1px rgba(59, 130, 246, 0.4)', transition: 'all 0.2s' }}
                onMouseOver={e => e.currentTarget.style.transform = 'translateY(-2px)'}
                onMouseOut={e => e.currentTarget.style.transform = 'translateY(0)'}
              >
                Đóng
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Hidden Print Area */}
      <div style={{ position: 'absolute', top: '-9999px', left: '-9999px', width: '800px' }}>
        <div ref={ticketRef}>
          <TicketTemplate booking={printingBooking} profile={profile} />
        </div>
      </div>
    </div>
  );
};

export default ProfilePage;
