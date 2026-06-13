import React, { useState, useEffect } from 'react';
import { 
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer 
} from 'recharts';
import { FaMoneyBillWave, FaMapMarkedAlt, FaUsers, FaShoppingCart } from 'react-icons/fa';
import api from '../../api/axiosConfig';

interface DashboardStats {
  totalRevenue: number;
  totalBookings: number;
  totalTours: number;
  totalUsers: number;
  revenueByMonth: { month: string; revenue: number }[];
  topTours: { tourId: number; tourTitle: string; totalBookings: number; revenue: number }[];
}

const Dashboard: React.FC = () => {
  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchStats = async () => {
      try {
        const response = await api.get('/admin/dashboard/stats');
        setStats(response.data.data);
      } catch (error) {
        console.error("Failed to fetch dashboard stats", error);
      } finally {
        setLoading(false);
      }
    };
    fetchStats();
  }, []);

  if (loading) return <div>Đang tải thống kê...</div>;
  if (!stats) return <div>Lỗi tải dữ liệu</div>;

  return (
    <div className="admin-panel">
      <div className="admin-header">
        <h2>Tổng Quan Bảng Điều Khiển</h2>
      </div>
      
      {/* 4 Cards Thống Kê */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '20px', marginBottom: '40px' }}>
        
        <div style={{ background: '#fff', padding: '20px', borderRadius: '10px', boxShadow: '0 4px 6px rgba(0,0,0,0.05)', display: 'flex', alignItems: 'center', gap: '15px' }}>
          <div style={{ background: '#e0f2fe', padding: '15px', borderRadius: '50%', color: '#0284c7', fontSize: '1.5rem' }}>
            <FaMoneyBillWave />
          </div>
          <div>
            <h3 style={{ margin: 0, color: '#64748b', fontSize: '0.9rem' }}>Tổng Doanh Thu</h3>
            <p style={{ margin: '5px 0 0', fontSize: '1.4rem', fontWeight: 'bold', color: '#0f172a' }}>
              {stats.totalRevenue.toLocaleString()} VNĐ
            </p>
          </div>
        </div>

        <div style={{ background: '#fff', padding: '20px', borderRadius: '10px', boxShadow: '0 4px 6px rgba(0,0,0,0.05)', display: 'flex', alignItems: 'center', gap: '15px' }}>
          <div style={{ background: '#fef08a', padding: '15px', borderRadius: '50%', color: '#ca8a04', fontSize: '1.5rem' }}>
            <FaShoppingCart />
          </div>
          <div>
            <h3 style={{ margin: 0, color: '#64748b', fontSize: '0.9rem' }}>Đơn Đặt Tour</h3>
            <p style={{ margin: '5px 0 0', fontSize: '1.4rem', fontWeight: 'bold', color: '#0f172a' }}>
              {stats.totalBookings}
            </p>
          </div>
        </div>

        <div style={{ background: '#fff', padding: '20px', borderRadius: '10px', boxShadow: '0 4px 6px rgba(0,0,0,0.05)', display: 'flex', alignItems: 'center', gap: '15px' }}>
          <div style={{ background: '#dcfce7', padding: '15px', borderRadius: '50%', color: '#16a34a', fontSize: '1.5rem' }}>
            <FaMapMarkedAlt />
          </div>
          <div>
            <h3 style={{ margin: 0, color: '#64748b', fontSize: '0.9rem' }}>Số Lượng Tour</h3>
            <p style={{ margin: '5px 0 0', fontSize: '1.4rem', fontWeight: 'bold', color: '#0f172a' }}>
              {stats.totalTours}
            </p>
          </div>
        </div>

        <div style={{ background: '#fff', padding: '20px', borderRadius: '10px', boxShadow: '0 4px 6px rgba(0,0,0,0.05)', display: 'flex', alignItems: 'center', gap: '15px' }}>
          <div style={{ background: '#f3e8ff', padding: '15px', borderRadius: '50%', color: '#9333ea', fontSize: '1.5rem' }}>
            <FaUsers />
          </div>
          <div>
            <h3 style={{ margin: 0, color: '#64748b', fontSize: '0.9rem' }}>Số Khách Hàng</h3>
            <p style={{ margin: '5px 0 0', fontSize: '1.4rem', fontWeight: 'bold', color: '#0f172a' }}>
              {stats.totalUsers}
            </p>
          </div>
        </div>

      </div>

      {/* Biểu đồ */}
      <div style={{ background: '#fff', padding: '20px', borderRadius: '10px', boxShadow: '0 4px 6px rgba(0,0,0,0.05)' }}>
        <h3 style={{ marginTop: 0, marginBottom: '20px', color: '#334155' }}>Biểu Đồ Doanh Thu Theo Tháng</h3>
        <div style={{ width: '100%', height: 400 }}>
          <ResponsiveContainer>
            <BarChart
              data={stats.revenueByMonth}
              margin={{ top: 5, right: 30, left: 20, bottom: 5 }}
            >
              <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#e2e8f0" />
              <XAxis dataKey="month" stroke="#64748b" />
              <YAxis 
                stroke="#64748b" 
                tickFormatter={(value) => `${(value / 1000000).toFixed(0)}Tr`}
              />
              <Tooltip 
                formatter={(value: any) => [new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value), 'Doanh thu']}
                contentStyle={{ borderRadius: '8px', border: 'none', boxShadow: '0 4px 6px rgba(0,0,0,0.1)' }}
              />
              <Legend />
              <Bar dataKey="revenue" name="Doanh Thu" fill="#3b82f6" radius={[4, 4, 0, 0]} barSize={40} />
            </BarChart>
          </ResponsiveContainer>
        </div>
      </div>

      {/* Top Tours Table */}
      <div style={{ marginTop: '40px', background: '#fff', padding: '24px', borderRadius: '12px', boxShadow: '0 4px 6px rgba(0,0,0,0.05)' }}>
        <h3 style={{ marginTop: 0, marginBottom: '24px', color: '#1e293b', fontSize: '1.25rem', borderBottom: '2px solid #f1f5f9', paddingBottom: '16px' }}>
          Top 5 Tour Bán Chạy Nhất
        </h3>
        <div style={{ overflowX: 'auto' }}>
          <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
            <thead>
              <tr style={{ background: '#f8fafc', color: '#64748b', fontSize: '0.9rem', textTransform: 'uppercase' }}>
                <th style={{ padding: '16px', borderBottom: '1px solid #e2e8f0', borderRadius: '8px 0 0 0' }}>Tên Tour</th>
                <th style={{ padding: '16px', borderBottom: '1px solid #e2e8f0', textAlign: 'center' }}>Số Lượt Đặt</th>
                <th style={{ padding: '16px', borderBottom: '1px solid #e2e8f0', textAlign: 'right', borderRadius: '0 8px 0 0' }}>Doanh Thu</th>
              </tr>
            </thead>
            <tbody>
              {stats.topTours && stats.topTours.length > 0 ? (
                stats.topTours.map((tour, index) => (
                  <tr key={tour.tourId} style={{ borderBottom: '1px solid #f1f5f9', transition: 'background 0.2s' }}>
                    <td style={{ padding: '16px', color: '#334155', fontWeight: 500 }}>
                      <span style={{ display: 'inline-block', width: '24px', height: '24px', background: index < 3 ? '#fef08a' : '#f1f5f9', color: index < 3 ? '#ca8a04' : '#64748b', borderRadius: '50%', textAlign: 'center', lineHeight: '24px', marginRight: '12px', fontSize: '0.85rem', fontWeight: 'bold' }}>{index + 1}</span>
                      {tour.tourTitle}
                    </td>
                    <td style={{ padding: '16px', textAlign: 'center', color: '#475569' }}>
                      <span style={{ background: '#e0f2fe', color: '#0369a1', padding: '4px 12px', borderRadius: '9999px', fontSize: '0.85rem', fontWeight: 600 }}>{tour.totalBookings}</span>
                    </td>
                    <td style={{ padding: '16px', textAlign: 'right', color: '#0f172a', fontWeight: 'bold' }}>
                      {new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(tour.revenue)}
                    </td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan={3} style={{ padding: '32px', textAlign: 'center', color: '#94a3b8' }}>Chưa có dữ liệu Tour bán chạy</td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};

export default Dashboard;
