import React, { useState, useRef } from 'react';
import { FaCheckCircle, FaTimesCircle } from 'react-icons/fa';
import { useQuery } from '@tanstack/react-query';
import html2canvas from 'html2canvas';
import jsPDF from 'jspdf';
import { UserService } from '../services/UserService';
import { BookingService } from '../services/BookingService';
import TicketTemplate from '../components/TicketTemplate';

import ProfileSidebar from '../components/profile/ProfileSidebar';
import UserInfoTab from '../components/profile/UserInfoTab';
import ChangePasswordTab from '../components/profile/ChangePasswordTab';
import BillingTab from '../components/profile/BillingTab';
import BookingHistoryTab from '../components/profile/BookingHistoryTab';
import BookingDetailsModal from '../components/profile/BookingDetailsModal';

import '../styles/tourDetail.css';

const ProfilePage: React.FC = () => {
  const [activeTab, setActiveTab] = useState('profile');
  const [message, setMessage] = useState({ text: '', type: '' });
  const [selectedBooking, setSelectedBooking] = useState<any>(null);

  // PDF Print State
  const ticketRef = useRef<HTMLDivElement>(null);
  const [printingBooking, setPrintingBooking] = useState<any>(null);
  const [isGeneratingPDF, setIsGeneratingPDF] = useState(false);

  // React Query Fetching
  const { data: profile, isLoading: isProfileLoading } = useQuery({
    queryKey: ['profile'],
    queryFn: () => UserService.getMyProfile(),
  });

  const { data: bookings = [], isLoading: isBookingsLoading } = useQuery({
    queryKey: ['bookings'],
    queryFn: () => BookingService.getMyBookings(),
  });

  const triggerPrint = (booking: any) => {
    setPrintingBooking(booking);
    setIsGeneratingPDF(true);
    
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

  const renderMessage = () => {
    if (!message.text) return null;
    return (
      <div style={{ padding: '10px', marginBottom: '20px', borderRadius: '8px', background: message.type === 'success' ? '#dcfce7' : '#fee2e2', color: message.type === 'success' ? '#166534' : '#991b1b', display: 'flex', alignItems: 'center', gap: '8px' }}>
        {message.type === 'success' ? <FaCheckCircle /> : <FaTimesCircle />}
        {message.text}
      </div>
    );
  };

  return (
    <div className="container" style={{ paddingTop: '100px', paddingBottom: '50px', minHeight: 'calc(100vh - 100px)' }}>
      <div style={{ display: 'flex', gap: '30px', flexWrap: 'wrap' }}>
        
        <ProfileSidebar 
          activeTab={activeTab} 
          setActiveTab={setActiveTab} 
          clearMessage={() => setMessage({ text: '', type: '' })} 
        />

        <div style={{ flex: 1, minWidth: '300px', background: '#fff', padding: '30px', borderRadius: '12px', boxShadow: '0 4px 6px rgba(0,0,0,0.05)' }}>
          {renderMessage()}

          {activeTab === 'profile' && (
            isProfileLoading ? <p>Đang tải...</p> : 
            <UserInfoTab initialProfile={profile} setMessage={setMessage} />
          )}

          {activeTab === 'password' && <ChangePasswordTab setMessage={setMessage} />}

          {activeTab === 'billing' && (
            isBookingsLoading ? <p>Đang tải...</p> : 
            <BillingTab bookings={bookings} triggerPrint={triggerPrint} isGeneratingPDF={isGeneratingPDF} />
          )}

          {activeTab === 'bookings' && (
            isBookingsLoading ? <p>Đang tải...</p> : 
            <BookingHistoryTab 
              bookings={bookings} 
              setSelectedBooking={setSelectedBooking} 
              triggerPrint={triggerPrint} 
              isGeneratingPDF={isGeneratingPDF} 
              setMessage={setMessage} 
            />
          )}
        </div>
      </div>

      <BookingDetailsModal 
        selectedBooking={selectedBooking} 
        setSelectedBooking={setSelectedBooking} 
        profile={profile || {}} 
      />

      {/* Hidden Print Area */}
      <div style={{ position: 'absolute', top: '-9999px', left: '-9999px', width: '800px' }}>
        <div ref={ticketRef}>
          <TicketTemplate booking={printingBooking} profile={profile || {}} />
        </div>
      </div>
    </div>
  );
};

export default ProfilePage;
