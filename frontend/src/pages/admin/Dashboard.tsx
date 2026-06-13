import React, { useState, useEffect } from 'react';
import { 
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer 
} from 'recharts';
import { FaMoneyBillWave, FaMapMarkedAlt, FaUsers, FaShoppingCart, FaTrophy } from 'react-icons/fa';
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
  const [chartView, setChartView] = useState<'MONTH' | 'YEAR'>('MONTH');

  const yearlyData = React.useMemo(() => {
    if (!stats) return [];
    const yearMap: Record<string, number> = {};
    stats.revenueByMonth.forEach(item => {
      const year = item.month.split('-')[0];
      yearMap[year] = (yearMap[year] || 0) + item.revenue;
    });
    return Object.keys(yearMap).map(year => ({ month: year, revenue: yearMap[year] })).sort((a, b) => a.month.localeCompare(b.month));
  }, [stats]);

  const chartData = chartView === 'MONTH' ? (stats ? stats.revenueByMonth : []) : yearlyData;

  useEffect(() => {
    const fetchStats = async () => {
      try {
        const response: any = await api.get('/admin/dashboard/stats');
        setStats(response.data || response);
      } catch (error) {
        console.error("Failed to fetch dashboard stats", error);
      } finally {
        setLoading(false);
      }
    };
    fetchStats();
  }, []);

  if (loading) return <div style={{ padding: '40px', textAlign: 'center', fontSize: '1.2rem', color: '#64748b' }}>Đang tải thống kê...</div>;
  if (!stats) return <div style={{ padding: '40px', textAlign: 'center', fontSize: '1.2rem', color: '#ef4444' }}>Lỗi tải dữ liệu</div>;

  return (
    <div className="admin-panel" style={{ background: '#f8fafc', minHeight: '100vh', padding: '20px' }}>
      <style>
        {`
          .dashboard-card {
            transition: all 0.3s ease;
            position: relative;
            overflow: hidden;
            border: none !important;
          }
          .dashboard-card:hover {
            transform: translateY(-8px);
            box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04) !important;
          }
          .dashboard-card::after {
            content: '';
            position: absolute;
            top: -50%;
            right: -50%;
            width: 100%;
            height: 100%;
            background: radial-gradient(circle, rgba(255,255,255,0.2) 0%, rgba(255,255,255,0) 70%);
            transform: rotate(45deg);
            pointer-events: none;
          }
          .top-tour-row {
            transition: all 0.2s ease;
          }
          .top-tour-row:hover {
            background-color: #f1f5f9;
            transform: scale(1.005);
          }
          .glass-icon {
            background: rgba(255, 255, 255, 0.2);
            backdrop-filter: blur(10px);
            -webkit-backdrop-filter: blur(10px);
            border: 1px solid rgba(255, 255, 255, 0.3);
          }
          .gradient-text {
            background: linear-gradient(135deg, #1e293b 0%, #334155 100%);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
          }
        `}
      </style>

      <div className="admin-header" style={{ marginBottom: '30px' }}>
        <h2 className="gradient-text" style={{ fontSize: '2rem', fontWeight: '800', margin: 0 }}>Tổng Quan Bảng Điều Khiển</h2>
        <p style={{ color: '#64748b', marginTop: '8px' }}>Xin chào Admin, dưới đây là tình hình kinh doanh hiện tại.</p>
      </div>
      
      {/* 4 Cards Thống Kê */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))', gap: '24px', marginBottom: '40px' }}>
        
        {/* Doanh thu */}
        <div className="dashboard-card" style={{ background: 'linear-gradient(135deg, #3b82f6 0%, #2563eb 100%)', padding: '24px', borderRadius: '16px', display: 'flex', alignItems: 'center', gap: '20px', color: 'white', boxShadow: '0 10px 15px -3px rgba(59, 130, 246, 0.3)' }}>
          <div className="glass-icon" style={{ padding: '16px', borderRadius: '12px', fontSize: '1.8rem', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
            <FaMoneyBillWave />
          </div>
          <div>
            <h3 style={{ margin: 0, fontSize: '0.95rem', opacity: 0.9, fontWeight: 500, textTransform: 'uppercase', letterSpacing: '0.5px' }}>Tổng Doanh Thu</h3>
            <p style={{ margin: '8px 0 0', fontSize: '1.8rem', fontWeight: 'bold' }}>
              {stats.totalRevenue.toLocaleString()} <span style={{ fontSize: '1rem', opacity: 0.8 }}>VNĐ</span>
            </p>
          </div>
        </div>

        {/* Đơn đặt tour */}
        <div className="dashboard-card" style={{ background: 'linear-gradient(135deg, #f59e0b 0%, #d97706 100%)', padding: '24px', borderRadius: '16px', display: 'flex', alignItems: 'center', gap: '20px', color: 'white', boxShadow: '0 10px 15px -3px rgba(245, 158, 11, 0.3)' }}>
          <div className="glass-icon" style={{ padding: '16px', borderRadius: '12px', fontSize: '1.8rem', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
            <FaShoppingCart />
          </div>
          <div>
            <h3 style={{ margin: 0, fontSize: '0.95rem', opacity: 0.9, fontWeight: 500, textTransform: 'uppercase', letterSpacing: '0.5px' }}>Đơn Đặt Tour</h3>
            <p style={{ margin: '8px 0 0', fontSize: '1.8rem', fontWeight: 'bold' }}>
              {stats.totalBookings}
            </p>
          </div>
        </div>

        {/* Lượng Tour */}
        <div className="dashboard-card" style={{ background: 'linear-gradient(135deg, #10b981 0%, #059669 100%)', padding: '24px', borderRadius: '16px', display: 'flex', alignItems: 'center', gap: '20px', color: 'white', boxShadow: '0 10px 15px -3px rgba(16, 185, 129, 0.3)' }}>
          <div className="glass-icon" style={{ padding: '16px', borderRadius: '12px', fontSize: '1.8rem', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
            <FaMapMarkedAlt />
          </div>
          <div>
            <h3 style={{ margin: 0, fontSize: '0.95rem', opacity: 0.9, fontWeight: 500, textTransform: 'uppercase', letterSpacing: '0.5px' }}>Số Lượng Tour</h3>
            <p style={{ margin: '8px 0 0', fontSize: '1.8rem', fontWeight: 'bold' }}>
              {stats.totalTours}
            </p>
          </div>
        </div>

        {/* Khách hàng */}
        <div className="dashboard-card" style={{ background: 'linear-gradient(135deg, #8b5cf6 0%, #6d28d9 100%)', padding: '24px', borderRadius: '16px', display: 'flex', alignItems: 'center', gap: '20px', color: 'white', boxShadow: '0 10px 15px -3px rgba(139, 92, 246, 0.3)' }}>
          <div className="glass-icon" style={{ padding: '16px', borderRadius: '12px', fontSize: '1.8rem', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
            <FaUsers />
          </div>
          <div>
            <h3 style={{ margin: 0, fontSize: '0.95rem', opacity: 0.9, fontWeight: 500, textTransform: 'uppercase', letterSpacing: '0.5px' }}>Số Khách Hàng</h3>
            <p style={{ margin: '8px 0 0', fontSize: '1.8rem', fontWeight: 'bold' }}>
              {stats.totalUsers}
            </p>
          </div>
        </div>

      </div>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(400px, 1fr))', gap: '30px' }}>
        
        {/* Biểu đồ */}
        <div style={{ background: '#fff', padding: '30px', borderRadius: '16px', boxShadow: '0 4px 6px -1px rgba(0, 0, 0, 0.05), 0 2px 4px -1px rgba(0, 0, 0, 0.03)' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '25px' }}>
            <h3 style={{ margin: 0, color: '#1e293b', fontSize: '1.25rem', fontWeight: 700 }}>Biểu đồ Doanh Thu</h3>
            <select 
              value={chartView}
              onChange={(e) => setChartView(e.target.value as any)}
              style={{ background: '#f1f5f9', color: '#64748b', padding: '8px 16px', borderRadius: '8px', fontSize: '0.9rem', fontWeight: 600, border: 'none', cursor: 'pointer', outline: 'none' }}
            >
              <option value="MONTH">Theo Tháng</option>
              <option value="YEAR">Theo Năm</option>
            </select>
          </div>
          <div style={{ width: '100%', height: 400 }}>
            <ResponsiveContainer>
              <BarChart
                data={chartData}
                margin={{ top: 10, right: 10, left: 0, bottom: 0 }}
              >
                <defs>
                  <linearGradient id="colorRevenue" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#3b82f6" stopOpacity={1}/>
                    <stop offset="95%" stopColor="#8b5cf6" stopOpacity={0.8}/>
                  </linearGradient>
                </defs>
                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#f1f5f9" />
                <XAxis dataKey="month" stroke="#94a3b8" axisLine={false} tickLine={false} tick={{ fill: '#64748b', fontSize: 13 }} dy={10} />
                <YAxis 
                  stroke="#94a3b8" 
                  axisLine={false} 
                  tickLine={false}
                  tick={{ fill: '#64748b', fontSize: 13 }}
                  tickFormatter={(value) => (value / 1000000).toFixed(0) + 'Tr'}
                  dx={-10}
                />
                <Tooltip 
                  formatter={(value: any) => [new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value), 'Doanh thu']}
                  contentStyle={{ borderRadius: '12px', border: 'none', boxShadow: '0 10px 15px -3px rgba(0, 0, 0, 0.1)', padding: '12px' }}
                  cursor={{ fill: 'rgba(241, 245, 249, 0.4)' }}
                />
                <Legend iconType="circle" wrapperStyle={{ paddingTop: '20px' }} />
                <Bar dataKey="revenue" name="Doanh Thu" fill="url(#colorRevenue)" radius={[8, 8, 0, 0]} barSize={45} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* Top Tours Table */}
        <div style={{ background: '#fff', padding: '30px', borderRadius: '16px', boxShadow: '0 4px 6px -1px rgba(0, 0, 0, 0.05), 0 2px 4px -1px rgba(0, 0, 0, 0.03)' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '25px', paddingBottom: '16px', borderBottom: '1px solid #f1f5f9' }}>
            <div style={{ background: '#fef3c7', color: '#d97706', padding: '10px', borderRadius: '10px', fontSize: '1.2rem' }}>
              <FaTrophy />
            </div>
            <h3 style={{ margin: 0, color: '#1e293b', fontSize: '1.25rem', fontWeight: 700 }}>
              Top 5 Tour Bán Chạy Nhất
            </h3>
          </div>
          <div style={{ overflowX: 'auto' }}>
            <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
              <thead>
                <tr style={{ color: '#94a3b8', fontSize: '0.85rem', textTransform: 'uppercase', letterSpacing: '0.5px' }}>
                  <th style={{ padding: '0 16px 16px 16px', fontWeight: 600 }}>Tên Tour</th>
                  <th style={{ padding: '0 16px 16px 16px', fontWeight: 600, textAlign: 'center' }}>Lượt Đặt</th>
                  <th style={{ padding: '0 16px 16px 16px', fontWeight: 600, textAlign: 'right' }}>Doanh Thu</th>
                </tr>
              </thead>
              <tbody>
                {stats.topTours && stats.topTours.length > 0 ? (
                  stats.topTours.map((tour, index) => (
                    <tr key={tour.tourId} className="top-tour-row">
                      <td style={{ padding: '16px', color: '#334155', fontWeight: 600, display: 'flex', alignItems: 'center', gap: '12px', borderBottom: '1px solid #f8fafc' }}>
                        <span style={{ 
                          display: 'flex', alignItems: 'center', justifyContent: 'center', width: '28px', height: '28px', 
                          background: index === 0 ? 'linear-gradient(135deg, #fbbf24, #d97706)' : index === 1 ? 'linear-gradient(135deg, #94a3b8, #64748b)' : index === 2 ? 'linear-gradient(135deg, #d97706, #92400e)' : '#f1f5f9', 
                          color: index < 3 ? '#fff' : '#64748b', 
                          borderRadius: '50%', fontSize: '0.85rem', fontWeight: 'bold', flexShrink: 0,
                          boxShadow: index < 3 ? '0 2px 4px rgba(0,0,0,0.1)' : 'none'
                        }}>
                          {index + 1}
                        </span>
                        <span style={{ whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis', maxWidth: '200px' }} title={tour.tourTitle}>
                          {tour.tourTitle}
                        </span>
                      </td>
                      <td style={{ padding: '16px', textAlign: 'center', borderBottom: '1px solid #f8fafc' }}>
                        <span style={{ background: '#e0f2fe', color: '#0284c7', padding: '4px 12px', borderRadius: '20px', fontSize: '0.85rem', fontWeight: 700 }}>
                          {tour.totalBookings}
                        </span>
                      </td>
                      <td style={{ padding: '16px', textAlign: 'right', color: '#0f172a', fontWeight: 'bold', borderBottom: '1px solid #f8fafc' }}>
                        {new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(tour.revenue)}
                      </td>
                    </tr>
                  ))
                ) : (
                  <tr>
                    <td colSpan={3} style={{ padding: '40px 20px', textAlign: 'center', color: '#94a3b8', background: '#f8fafc', borderRadius: '12px' }}>
                      <div style={{ fontSize: '2rem', marginBottom: '10px', color: '#cbd5e1' }}><FaTrophy /></div>
                      Chưa có dữ liệu Tour bán chạy
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </div>

      </div>
    </div>
  );
};

export default Dashboard;
