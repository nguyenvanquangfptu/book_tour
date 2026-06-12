import React from 'react';
import '../styles/pages.css';

const AboutPage: React.FC = () => {
  const teamMembers = [
    {
      name: 'Nguyễn Văn A',
      role: 'Giám Đốc Điều Hành',
      image: 'https://images.unsplash.com/photo-1560250097-0b93528c311a?auto=format&fit=crop&w=300&q=80'
    },
    {
      name: 'Trần Thị B',
      role: 'Trưởng Phòng Điều Hành',
      image: 'https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?auto=format&fit=crop&w=300&q=80'
    },
    {
      name: 'Lê Văn C',
      role: 'Chuyên Gia Thiết Kế Tour',
      image: 'https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?auto=format&fit=crop&w=300&q=80'
    },
    {
      name: 'Phạm Thị D',
      role: 'Chăm Sóc Khách Hàng',
      image: 'https://images.unsplash.com/photo-1580489944761-15a19d654956?auto=format&fit=crop&w=300&q=80'
    }
  ];

  return (
    <div className="page-wrapper">
      {/* Page Header */}
      <div className="page-header" style={{ backgroundImage: "url('https://images.unsplash.com/photo-1469854523086-cc02fe5d8800?auto=format&fit=crop&w=1920&q=80')" }}>
        <div className="page-overlay"></div>
        <h1 className="page-title animate-fade-in">Về Chúng Tôi</h1>
      </div>

      {/* About Section */}
      <div className="about-section container">
        <div className="about-grid">
          <div className="about-content animate-slide-up">
            <h2>Hành trình mang đến những trải nghiệm tuyệt vời</h2>
            <p>
              BookingTour được thành lập với sứ mệnh mang đến cho khách hàng những chuyến đi không chỉ là du lịch, 
              mà còn là những trải nghiệm văn hóa, khám phá bản thân và tận hưởng cuộc sống một cách trọn vẹn nhất.
            </p>
            <p>
              Với hơn 10 năm kinh nghiệm trong ngành du lịch, chúng tôi tự hào là người bạn đồng hành tin cậy của 
              hàng nghìn du khách trên mọi nẻo đường, từ những bãi biển xanh ngắt đến những ngọn núi hùng vĩ.
            </p>
            <div style={{ marginTop: '32px', display: 'flex', gap: '24px' }}>
              <div>
                <h3 style={{ fontSize: '2.5rem', color: 'var(--primary)', marginBottom: '8px' }}>10k+</h3>
                <p style={{ color: 'var(--text-secondary)', fontWeight: '600' }}>Khách hàng hài lòng</p>
              </div>
              <div>
                <h3 style={{ fontSize: '2.5rem', color: 'var(--primary)', marginBottom: '8px' }}>500+</h3>
                <p style={{ color: 'var(--text-secondary)', fontWeight: '600' }}>Điểm đến tuyệt đẹp</p>
              </div>
            </div>
          </div>
          <div className="about-image animate-slide-up" style={{ animationDelay: '0.2s' }}>
            <img src="https://images.unsplash.com/photo-1539635278303-d4002c07eae3?auto=format&fit=crop&w=800&q=80" alt="Travel Experience" />
          </div>
        </div>

        {/* Why Choose Us - Reuse from home features */}
        <div style={{ marginTop: '100px', textAlign: 'center' }}>
          <h2 style={{ fontSize: '2.5rem', color: 'var(--primary-dark)', marginBottom: '48px' }}>Tại sao chọn BookingTour?</h2>
          <div className="features-grid">
            <div className="feature-card glass-card">
              <h3>Giá Cả Tốt Nhất</h3>
              <p>Cam kết mang đến cho bạn mức giá cạnh tranh và nhiều ưu đãi hấp dẫn nhất thị trường.</p>
            </div>
            <div className="feature-card glass-card">
              <h3>Dịch Vụ Chuẩn 5 Sao</h3>
              <p>Đội ngũ hướng dẫn viên chuyên nghiệp, tận tâm và am hiểu điểm đến.</p>
            </div>
            <div className="feature-card glass-card">
              <h3>Hỗ Trợ 24/7</h3>
              <p>Luôn sẵn sàng hỗ trợ và giải đáp mọi thắc mắc của bạn bất cứ lúc nào.</p>
            </div>
          </div>
        </div>

        {/* Team Section */}
        <div style={{ marginTop: '100px' }}>
          <h2 style={{ fontSize: '2.5rem', color: 'var(--primary-dark)', marginBottom: '16px', textAlign: 'center' }}>Đội Ngũ Của Chúng Tôi</h2>
          <p style={{ textAlign: 'center', color: 'var(--text-secondary)', fontSize: '1.1rem', maxWidth: '600px', margin: '0 auto' }}>
            Những con người đam mê du lịch và luôn cống hiến hết mình để tạo ra những hành trình đáng nhớ cho bạn.
          </p>
          
          <div className="team-grid">
            {teamMembers.map((member, index) => (
              <div key={index} className="team-member animate-slide-up" style={{ animationDelay: `${index * 0.1}s` }}>
                <img src={member.image} alt={member.name} />
                <h4>{member.name}</h4>
                <p>{member.role}</p>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
};

export default AboutPage;
