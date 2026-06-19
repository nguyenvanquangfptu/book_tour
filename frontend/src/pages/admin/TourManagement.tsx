import React, { useState, useEffect } from 'react';
import { FaEdit, FaTrash, FaPlus, FaMapMarkerAlt, FaClock, FaSearch, FaFilter } from 'react-icons/fa';
import Swal from 'sweetalert2';
import { TourService } from '../../services/TourService';
import api from '../../api/axiosConfig';
import { formatPrice } from '../../utils/formatPrice';

const TourManagement: React.FC = () => {
  const [tours, setTours] = useState<any[]>([]);
  const [accommodations, setAccommodations] = useState<any[]>([]);
  const [utilities, setUtilities] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [activeTab, setActiveTab] = useState('general'); // 'general', 'images', 'details'
  const [editingTour, setEditingTour] = useState<any>(null);

  const [searchTerm, setSearchTerm] = useState('');
  const [priceFilter, setPriceFilter] = useState('ALL');
  const [destinationFilter, setDestinationFilter] = useState('ALL');
  const [statusFilter, setStatusFilter] = useState('ALL'); // 'ALL', 'ACTIVE', 'INACTIVE', 'SOLD_OUT'

  // Lấy danh sách địa điểm độc nhất từ tours
  const uniqueDestinations = Array.from(new Set(tours.map(tour => tour.destination).filter(d => d))).sort();

  const filteredTours = tours.filter(tour => {
    const matchSearch = tour.title.toLowerCase().includes(searchTerm.toLowerCase());
    
    let matchPrice = true;
    if (priceFilter === 'UNDER_5M') matchPrice = tour.price < 5000000;
    else if (priceFilter === '5M_TO_10M') matchPrice = tour.price >= 5000000 && tour.price <= 10000000;
    else if (priceFilter === 'OVER_10M') matchPrice = tour.price > 10000000;

    let matchDestination = true;
    if (destinationFilter !== 'ALL') {
      matchDestination = tour.destination === destinationFilter;
    }

    let matchStatus = true;
    if (statusFilter !== 'ALL') {
      matchStatus = tour.status === statusFilter;
    }

    return matchSearch && matchPrice && matchDestination && matchStatus;
  });

  const [formData, setFormData] = useState({
    title: '',
    description: '',
    price: 0,
    duration: '',
    destination: '',
    imageUrl: '',
    images: [] as string[],
    accommodationIds: [] as number[],
    maxPeople: 1,
    utilityIds: [] as number[],
    highlights: [''] as string[],
    itinerary: [] as {day: string, title: string, description: string}[],
    status: 'INACTIVE'
  });
  const [uploading, setUploading] = useState(false);
  const [uploadingCover, setUploadingCover] = useState(false);

  const fetchTours = async () => {
    try {
      const data = await TourService.getTours(0, 100);
      if (data && data.content) {
        setTours(data.content);
      }
    } catch (error) {
      console.error('Failed to fetch tours', error);
    } finally {
      setLoading(false);
    }
  };

  const fetchAccommodations = async () => {
    try {
      const response: any = await api.get('/accommodations');
      // Do axios interceptor trả thẳng về data, nên response chính là mảng
      if (Array.isArray(response)) {
        setAccommodations(response);
      } else if (response && response.data) {
        setAccommodations(response.data);
      }
    } catch (error) {
      console.error('Failed to fetch accommodations', error);
    }
  };

  const fetchUtilities = async () => {
    try {
      const response: any = await api.get('/utilities');
      if (Array.isArray(response)) {
        setUtilities(response);
      } else if (response && response.data) {
        setUtilities(response.data);
      }
    } catch (error) {
      console.error('Failed to fetch utilities', error);
    }
  };

  useEffect(() => {
    fetchTours();
    fetchAccommodations();
    fetchUtilities();
  }, []);

  const handleAccommodationToggle = (id: number) => {
    setFormData(prev => {
      const newAccIds = prev.accommodationIds.includes(id) 
        ? prev.accommodationIds.filter(aid => aid !== id)
        : [...prev.accommodationIds, id];
      return { ...prev, accommodationIds: newAccIds };
    });
  };

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const handleUtilityToggle = (id: number) => {
    setFormData(prev => {
      const newUtilityIds = prev.utilityIds.includes(id) 
        ? prev.utilityIds.filter(uid => uid !== id)
        : [...prev.utilityIds, id];
      return { ...prev, utilityIds: newUtilityIds };
    });
  };

  const handleAddHighlight = () => {
    setFormData(prev => ({ ...prev, highlights: [...prev.highlights, ''] }));
  };

  const handleHighlightChange = (index: number, value: string) => {
    const newHighlights = [...formData.highlights];
    newHighlights[index] = value;
    setFormData(prev => ({ ...prev, highlights: newHighlights }));
  };

  const handleRemoveHighlight = (index: number) => {
    const newHighlights = [...formData.highlights];
    newHighlights.splice(index, 1);
    setFormData(prev => ({ ...prev, highlights: newHighlights }));
  };

  const handleAddItinerary = () => {
    setFormData(prev => ({ ...prev, itinerary: [...prev.itinerary, { day: '', title: '', description: '' }] }));
  };

  const handleItineraryChange = (index: number, field: string, value: string) => {
    const newItinerary = [...formData.itinerary];
    newItinerary[index] = { ...newItinerary[index], [field]: value };
    setFormData(prev => ({ ...prev, itinerary: newItinerary }));
  };

  const handleRemoveItinerary = (index: number) => {
    const newItinerary = [...formData.itinerary];
    newItinerary.splice(index, 1);
    setFormData(prev => ({ ...prev, itinerary: newItinerary }));
  };

  const handleAddNew = () => {
    setEditingTour(null);
    setFormData({ title: '', description: '', price: 0, duration: '', destination: '', imageUrl: '', images: [], accommodationIds: [], maxPeople: 1, utilityIds: [], highlights: [''], itinerary: [{day: '', title: '', description: ''}], status: 'INACTIVE' });
    setShowModal(true);
  };

  const handleEdit = (tour: any) => {
    setEditingTour(tour);
    setFormData({
      title: tour.title,
      description: tour.description || '',
      price: tour.price,
      duration: tour.duration || '',
      destination: tour.destination || '',
      imageUrl: tour.imageUrl || '',
      images: tour.images || [],
      accommodationIds: tour.accommodations && tour.accommodations.length > 0 ? [tour.accommodations[0].id] : [],
      maxPeople: tour.maxPeople || 1,
      utilityIds: tour.utilities ? tour.utilities.map((u: any) => u.id) : [],
      highlights: tour.highlights && tour.highlights.length > 0 ? tour.highlights : [''],
      itinerary: tour.itinerary && tour.itinerary.length > 0 ? tour.itinerary : [{day: '', title: '', description: ''}],
      status: tour.status || 'INACTIVE'
    });
    setActiveTab('general');
    setShowModal(true);
  };

  const handleFileChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
    if (!e.target.files || e.target.files.length === 0) return;
    
    setUploading(true);
    const uploadData = new FormData();
    Array.from(e.target.files).forEach(file => {
      uploadData.append('files', file);
    });

    try {
      const response = await api.post('/upload/multiple', uploadData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      });
      // response chính là ApiResponse (do axios interceptor đã lấy response.data)
      // Nên response.data chính là List<String> urls
      const urls = Array.isArray(response.data) ? response.data : (response.data?.data || []);
      setFormData(prev => ({
        ...prev,
        images: [...prev.images, ...urls]
      }));
    } catch (error: any) {
      console.error('Lỗi khi tải ảnh lên:', error);
      const errorMessage = error.response?.data?.message || 'Tải ảnh thất bại. Vui lòng kiểm tra lại cấu hình Cloudinary trên server.';
      Swal.fire({
        icon: 'error',
        title: 'Lỗi',
        text: errorMessage,
        confirmButtonColor: '#3b82f6'
      });
    } finally {
      setUploading(false);
    }
  };

  const handleCoverChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
    if (!e.target.files || e.target.files.length === 0) return;
    
    setUploadingCover(true);
    const uploadData = new FormData();
    uploadData.append('file', e.target.files[0]);

    try {
      const response = await api.post('/upload', uploadData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      });
      // response chính là ApiResponse
      const url = response.data;
      setFormData(prev => ({ ...prev, imageUrl: url }));
    } catch (error: any) {
      console.error('Lỗi khi tải ảnh bìa lên:', error);
      const errorMessage = error.response?.data?.message || 'Tải ảnh bìa thất bại.';
      Swal.fire({
        icon: 'error',
        title: 'Lỗi',
        text: errorMessage,
        confirmButtonColor: '#3b82f6'
      });
    } finally {
      setUploadingCover(false);
    }
  };

  const removeImage = (index: number) => {
    const newImages = [...formData.images];
    newImages.splice(index, 1);
    setFormData(prev => ({ ...prev, images: newImages }));
  };

  const handleDelete = async (id: number) => {
    const result = await Swal.fire({
      title: 'Xác nhận xóa?',
      text: "Bạn có chắc chắn muốn xóa Tour này?",
      icon: 'warning',
      showCancelButton: true,
      confirmButtonColor: '#ef4444',
      cancelButtonColor: '#94a3b8',
      confirmButtonText: 'Xóa',
      cancelButtonText: 'Hủy',
      customClass: {
        confirmButton: 'rounded-button',
        cancelButton: 'rounded-button'
      }
    });

    if (result.isConfirmed) {
      try {
        await TourService.deleteTour(id);
        fetchTours();
        Swal.fire('Thành công!', 'Tour đã bị xóa.', 'success');
      } catch (error: any) {
        console.error('Lỗi khi xóa', error);
        const errorMsg = error.response?.data?.message || 'Xóa thất bại';
        Swal.fire({
          icon: 'error',
          title: 'Lỗi',
          text: errorMsg,
          confirmButtonColor: '#3b82f6'
        });
      }
    }
  };

  const handleStatusChange = async (tour: any, newStatus: string) => {
    try {
      const submitData = {
        title: tour.title,
        description: tour.description || '',
        price: tour.price,
        duration: tour.duration || '',
        destination: tour.destination || '',
        imageUrl: tour.imageUrl || '',
        images: tour.images || [],
        accommodationIds: tour.accommodations && tour.accommodations.length > 0 ? [tour.accommodations[0].id] : [],
        maxPeople: tour.maxPeople || 1,
        utilityIds: tour.utilities ? tour.utilities.map((u: any) => u.id) : [],
        highlights: tour.highlights && tour.highlights.length > 0 ? tour.highlights : [''],
        itinerary: tour.itinerary && tour.itinerary.length > 0 ? tour.itinerary : [{day: '', title: '', description: ''}],
        status: newStatus
      };
      await TourService.updateTour(tour.id, submitData);
      
      // Cập nhật state local
      setTours(prev => prev.map(t => t.id === tour.id ? { ...t, status: newStatus } : t));
      
      const Toast = Swal.mixin({
        toast: true,
        position: "top-end",
        showConfirmButton: false,
        timer: 3000,
        timerProgressBar: true,
      });
      Toast.fire({
        icon: "success",
        title: "Cập nhật trạng thái thành công"
      });
    } catch (error: any) {
      console.error('Lỗi khi cập nhật trạng thái', error);
      const errorMsg = error.response?.data?.message || 'Cập nhật trạng thái thất bại';
      Swal.fire({
        icon: 'error',
        title: 'Lỗi',
        text: errorMsg,
        confirmButtonColor: '#3b82f6'
      });
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      const submitData = {
        ...formData
      };
      
      if (editingTour) {
        await TourService.updateTour(editingTour.id, submitData);
        Swal.fire('Thành công!', 'Cập nhật Tour thành công.', 'success');
      } else {
        await TourService.createTour(submitData);
        Swal.fire('Thành công!', 'Tạo Tour mới thành công.', 'success');
      }
      setShowModal(false);
      fetchTours();
    } catch (error: any) {
      console.error('Lỗi khi lưu Tour', error);
      const errorMsg = error.response?.data?.message || 'Lưu thất bại. Kiểm tra dữ liệu nhập.';
      Swal.fire({
        icon: 'error',
        title: 'Lỗi',
        text: errorMsg,
        confirmButtonColor: '#3b82f6'
      });
    }
  };

  return (
    <div className="admin-panel" style={{ background: '#f8fafc', minHeight: '100vh', padding: '30px' }}>
      <style>
        {`
          /* Admin Panel Layout Enhancements */
          .admin-panel {
            background: #ffffff;
            border-radius: 20px;
            padding: 32px;
            box-shadow: 0 20px 40px -15px rgba(0, 0, 0, 0.05), 0 0 10px rgba(0, 0, 0, 0.01);
            border: 1px solid rgba(255, 255, 255, 0.8);
          }
          
          /* Zebra Striping & Hover */
          .tour-row {
            transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
            background-color: #ffffff;
          }
          .tour-row:nth-child(even) {
            background-color: #fcfcfd;
          }
          .tour-row:hover {
            background-color: #f8fafc;
            transform: translateY(-2px);
            box-shadow: 0 10px 20px -10px rgba(0,0,0,0.08);
            position: relative;
            z-index: 2;
          }
          
          /* Thumbnail Glow */
          .tour-thumbnail {
            width: 110px;
            height: 75px;
            border-radius: 12px;
            overflow: hidden;
            box-shadow: 0 8px 16px -4px rgba(0,0,0,0.1), 0 0 0 1px rgba(0,0,0,0.02);
            transition: all 0.3s ease;
          }
          .tour-row:hover .tour-thumbnail {
            box-shadow: 0 12px 24px -6px rgba(59, 130, 246, 0.25), 0 0 0 1px rgba(59, 130, 246, 0.1);
          }
          
          /* Minimalist Action Buttons (Appear on Hover) */
          .actions-container {
            opacity: 0;
            transform: translateX(10px);
            transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
          }
          .tour-row:hover .actions-container {
            opacity: 1;
            transform: translateX(0);
          }
          
          .action-btn {
            width: 36px;
            height: 36px;
            border-radius: 10px;
            border: none;
            cursor: pointer;
            display: inline-flex;
            align-items: center;
            justify-content: center;
            transition: all 0.2s ease;
            font-size: 1.1rem;
          }
          .btn-edit-modern {
            background: #f1f5f9;
            color: #64748b;
          }
          .btn-edit-modern:hover {
            background: #eff6ff;
            color: #3b82f6;
            transform: scale(1.1);
          }
          .btn-delete-modern {
            background: #f1f5f9;
            color: #64748b;
          }
          .btn-delete-modern:hover {
            background: #fef2f2;
            color: #ef4444;
            transform: scale(1.1);
          }
          
          /* Add Button */
          .btn-add-modern {
            background: linear-gradient(135deg, #1e293b 0%, #334155 100%);
            color: white;
            padding: 12px 24px;
            border-radius: 12px;
            font-weight: 600;
            border: none;
            cursor: pointer;
            display: flex;
            align-items: center;
            gap: 8px;
            transition: all 0.3s ease;
            box-shadow: 0 10px 15px -3px rgba(30, 41, 59, 0.3);
          }
          .btn-add-modern:hover {
            transform: translateY(-3px);
            box-shadow: 0 15px 20px -3px rgba(30, 41, 59, 0.4);
            background: linear-gradient(135deg, #0f172a 0%, #1e293b 100%);
          }
          
          /* Modern Input */
          .modern-input {
            width: 100%;
            padding: 12px 16px;
            border: 1px solid #e2e8f0;
            border-radius: 10px;
            font-size: 0.95rem;
            transition: all 0.2s;
            background: #f8fafc;
            color: #1e293b;
          }
          .modern-input:focus {
            outline: none;
            border-color: #cbd5e1;
            background: #ffffff;
            box-shadow: 0 0 0 4px rgba(226, 232, 240, 0.5);
          }
          
          /* Gradient Text */
          .gradient-text {
            background: linear-gradient(135deg, #0f172a 0%, #334155 100%);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
          }
          
          /* Table Styles */
          .modern-table th {
            color: #64748b;
            font-size: 0.8rem;
            text-transform: uppercase;
            letter-spacing: 0.05em;
            font-weight: 700;
            padding: 20px 24px;
            border-bottom: 2px solid #f1f5f9;
            background: #ffffff;
          }
          .modern-table td {
            padding: 20px 24px;
            vertical-align: middle;
            border-bottom: 1px solid #f8fafc;
          }
        `}
      </style>

      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '30px' }}>
        <div>
          <h2 className="gradient-text" style={{ fontSize: '2rem', fontWeight: '800', margin: 0 }}>Quản lý Tour Du Lịch</h2>
          <p style={{ color: '#64748b', marginTop: '8px' }}>Tạo, chỉnh sửa và quản lý danh sách các Tour trên hệ thống.</p>
        </div>
        <button className="btn-add-modern" onClick={handleAddNew}>
          <FaPlus /> Thêm Tour Mới
        </button>
      </div>

      {loading ? (
        <div style={{ padding: '60px', textAlign: 'center', color: '#94a3b8', fontSize: '1.2rem', fontWeight: 500 }}>
          <div style={{ width: '40px', height: '40px', border: '3px solid #e2e8f0', borderTopColor: '#3b82f6', borderRadius: '50%', animation: 'spin 1s linear infinite', margin: '0 auto 15px' }}></div>
          Đang tải dữ liệu...
        </div>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
          {/* Status Tabs */}
          <div style={{ display: 'flex', gap: '10px', marginBottom: '10px' }}>
            <button 
              onClick={() => setStatusFilter('ALL')} 
              style={{ padding: '10px 20px', borderRadius: '30px', fontWeight: 600, fontSize: '0.9rem', cursor: 'pointer', transition: 'all 0.2s', border: 'none', background: statusFilter === 'ALL' ? '#1e293b' : '#fff', color: statusFilter === 'ALL' ? '#fff' : '#64748b', boxShadow: statusFilter === 'ALL' ? '0 4px 6px rgba(30, 41, 59, 0.2)' : '0 2px 4px rgba(0,0,0,0.05)' }}
            >
              Tất cả ({tours.length})
            </button>
            <button 
              onClick={() => setStatusFilter('ACTIVE')} 
              style={{ padding: '10px 20px', borderRadius: '30px', fontWeight: 600, fontSize: '0.9rem', cursor: 'pointer', transition: 'all 0.2s', border: 'none', background: statusFilter === 'ACTIVE' ? '#16a34a' : '#fff', color: statusFilter === 'ACTIVE' ? '#fff' : '#64748b', boxShadow: statusFilter === 'ACTIVE' ? '0 4px 6px rgba(22, 163, 74, 0.2)' : '0 2px 4px rgba(0,0,0,0.05)' }}
            >
              Đang bán ({tours.filter(t => t.status === 'ACTIVE').length})
            </button>
            <button 
              onClick={() => setStatusFilter('SOLD_OUT')} 
              style={{ padding: '10px 20px', borderRadius: '30px', fontWeight: 600, fontSize: '0.9rem', cursor: 'pointer', transition: 'all 0.2s', border: 'none', background: statusFilter === 'SOLD_OUT' ? '#ef4444' : '#fff', color: statusFilter === 'SOLD_OUT' ? '#fff' : '#64748b', boxShadow: statusFilter === 'SOLD_OUT' ? '0 4px 6px rgba(239, 68, 68, 0.2)' : '0 2px 4px rgba(0,0,0,0.05)' }}
            >
              Hết chỗ ({tours.filter(t => t.status === 'SOLD_OUT').length})
            </button>
            <button 
              onClick={() => setStatusFilter('INACTIVE')} 
              style={{ padding: '10px 20px', borderRadius: '30px', fontWeight: 600, fontSize: '0.9rem', cursor: 'pointer', transition: 'all 0.2s', border: 'none', background: statusFilter === 'INACTIVE' ? '#64748b' : '#fff', color: statusFilter === 'INACTIVE' ? '#fff' : '#64748b', boxShadow: statusFilter === 'INACTIVE' ? '0 4px 6px rgba(100, 116, 139, 0.2)' : '0 2px 4px rgba(0,0,0,0.05)' }}
            >
              Bản nháp ({tours.filter(t => t.status === 'INACTIVE').length})
            </button>
          </div>

          {/* Bộ lọc */}
          <div style={{ display: 'flex', gap: '16px', background: '#fff', padding: '20px', borderRadius: '16px', boxShadow: '0 4px 6px -1px rgba(0, 0, 0, 0.05)' }}>
            <div style={{ flex: 1, position: 'relative' }}>
              <FaSearch style={{ position: 'absolute', left: '16px', top: '50%', transform: 'translateY(-50%)', color: '#94a3b8' }} />
              <input 
                type="text" 
                placeholder="Tìm kiếm theo tên Tour..." 
                className="modern-input" 
                style={{ paddingLeft: '44px' }}
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
              />
            </div>
            <div style={{ width: '220px', position: 'relative' }}>
              <FaMapMarkerAlt style={{ position: 'absolute', left: '16px', top: '50%', transform: 'translateY(-50%)', color: '#94a3b8', zIndex: 1 }} />
              <select 
                className="modern-input" 
                style={{ paddingLeft: '44px', cursor: 'pointer', appearance: 'none' }}
                value={destinationFilter}
                onChange={(e) => setDestinationFilter(e.target.value)}
              >
                <option value="ALL">Tất cả địa điểm</option>
                {uniqueDestinations.map((dest: any) => (
                  <option key={dest} value={dest}>{dest}</option>
                ))}
              </select>
            </div>
            <div style={{ width: '250px', position: 'relative' }}>
              <FaFilter style={{ position: 'absolute', left: '16px', top: '50%', transform: 'translateY(-50%)', color: '#94a3b8', zIndex: 1 }} />
              <select 
                className="modern-input" 
                style={{ paddingLeft: '44px', cursor: 'pointer', appearance: 'none' }}
                value={priceFilter}
                onChange={(e) => setPriceFilter(e.target.value)}
              >
                <option value="ALL">Tất cả mức giá</option>
                <option value="UNDER_5M">Dưới 5.000.000 VNĐ</option>
                <option value="5M_TO_10M">5.000.000 - 10.000.000 VNĐ</option>
                <option value="OVER_10M">Trên 10.000.000 VNĐ</option>
              </select>
            </div>
          </div>

          <div style={{ background: '#fff', borderRadius: '20px', boxShadow: '0 10px 25px -5px rgba(0, 0, 0, 0.05), 0 8px 10px -6px rgba(0, 0, 0, 0.01)', overflow: 'hidden', border: '1px solid rgba(226, 232, 240, 0.8)' }}>
            <div style={{ overflowX: 'auto' }}>
              <table className="modern-table" style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
                <thead>
                  <tr>
                    <th>Mã</th>
                    <th>Hình ảnh</th>
                    <th>Tên Tour</th>
                    <th>Giá</th>
                    <th>Thời gian</th>
                    <th>Trạng thái</th>
                    <th style={{ textAlign: 'right' }}>Hành động</th>
                  </tr>
                </thead>
                <tbody>
                  {filteredTours.length === 0 ? (
                    <tr>
                      <td colSpan={7} style={{ padding: '60px', textAlign: 'center', color: '#94a3b8', fontSize: '1.1rem' }}>
                        Không tìm thấy Tour nào phù hợp với bộ lọc.
                      </td>
                    </tr>
                  ) : filteredTours.map(tour => (
                  <tr key={tour.id} className="tour-row">
                    <td style={{ color: '#94a3b8', fontWeight: 600, fontSize: '0.9rem' }}>#{tour.id}</td>
                    <td>
                      <div className="tour-thumbnail">
                        <img 
                          src={tour.imageUrl || 'https://images.unsplash.com/photo-1504280390467-33923018e6d0?auto=format&fit=crop&w=200&q=80'} 
                          alt="Tour" 
                          style={{ width: '100%', height: '100%', objectFit: 'cover' }}
                          onError={(e) => {
                            const target = e.target as HTMLImageElement;
                            target.onerror = null;
                            target.src = 'https://images.unsplash.com/photo-1504280390467-33923018e6d0?auto=format&fit=crop&w=200&q=80';
                          }}
                        />
                      </div>
                    </td>
                    <td>
                      <div style={{ maxWidth: '320px' }}>
                        <h4 style={{ margin: '0 0 6px 0', color: '#0f172a', fontSize: '1.05rem', fontWeight: 700, lineHeight: 1.3, display: '-webkit-box', WebkitLineClamp: 2, WebkitBoxOrient: 'vertical', overflow: 'hidden' }} title={tour.title}>
                          {tour.title}
                        </h4>
                        {tour.destination && (
                          <div style={{ display: 'flex', alignItems: 'center', gap: '6px', color: '#64748b', fontSize: '0.85rem', fontWeight: 500 }}>
                            <FaMapMarkerAlt style={{ color: '#94a3b8' }} /> {tour.destination}
                          </div>
                        )}
                      </div>
                    </td>
                    <td style={{ whiteSpace: 'nowrap' }}>
                      <div style={{ color: '#16a34a', fontWeight: 800, fontSize: '1.1rem', letterSpacing: '-0.5px' }}>
                        {formatPrice(tour.price)}
                      </div>
                    </td>
                    <td style={{ whiteSpace: 'nowrap' }}>
                      <div style={{ display: 'inline-flex', alignItems: 'center', gap: '8px', color: '#475569', fontWeight: 600, fontSize: '0.9rem', background: '#f8fafc', padding: '6px 14px', borderRadius: '30px', border: '1px solid #e2e8f0' }}>
                        <FaClock style={{ color: '#94a3b8' }} />
                        {tour.duration || 'N/A'}
                      </div>
                    </td>
                    <td>
                      <select 
                        value={tour.status}
                        onChange={(e) => handleStatusChange(tour, e.target.value)}
                        style={{
                          display: 'inline-flex',
                          alignItems: 'center',
                          justifyContent: 'center',
                          padding: '6px 14px',
                          borderRadius: '30px',
                          fontSize: '0.85rem',
                          fontWeight: 700,
                          backgroundColor: tour.status === 'ACTIVE' ? 'rgba(22, 163, 74, 0.1)' : tour.status === 'SOLD_OUT' ? 'rgba(239, 68, 68, 0.1)' : 'rgba(100, 116, 139, 0.1)',
                          color: tour.status === 'ACTIVE' ? '#16a34a' : tour.status === 'SOLD_OUT' ? '#ef4444' : '#64748b',
                          border: `1px solid ${tour.status === 'ACTIVE' ? 'rgba(22, 163, 74, 0.2)' : tour.status === 'SOLD_OUT' ? 'rgba(239, 68, 68, 0.2)' : 'rgba(100, 116, 139, 0.2)'}`,
                          cursor: 'pointer',
                          outline: 'none',
                          appearance: 'none', // hide native arrow on some browsers for cleaner look
                          textAlign: 'center'
                        }}
                      >
                        <option value="ACTIVE" style={{ color: '#000', backgroundColor: '#fff' }}>Đang bán</option>
                        <option value="SOLD_OUT" style={{ color: '#000', backgroundColor: '#fff' }}>Hết chỗ</option>
                        <option value="INACTIVE" style={{ color: '#000', backgroundColor: '#fff' }}>Bản nháp</option>
                      </select>
                    </td>
                    <td style={{ textAlign: 'right' }}>
                      <div className="actions-container" style={{ display: 'inline-flex', gap: '8px', justifyContent: 'flex-end' }}>
                        <button className="action-btn btn-edit-modern" onClick={() => handleEdit(tour)} title="Sửa Tour">
                          <FaEdit />
                        </button>
                        <button className="action-btn btn-delete-modern" onClick={() => handleDelete(tour.id)} title="Xóa Tour">
                          <FaTrash />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
              </table>
            </div>
          </div>
        </div>
      )}

      {/* Modal Thêm/Sửa */}
      {showModal && (
        <div style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, background: 'rgba(15, 23, 42, 0.6)', backdropFilter: 'blur(4px)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000, padding: '20px' }}>
          <div style={{ background: '#fff', borderRadius: '20px', width: '100%', maxWidth: '600px', maxHeight: '90vh', overflowY: 'auto', boxShadow: '0 25px 50px -12px rgba(0, 0, 0, 0.25)' }}>
            <div style={{ padding: '24px 30px', borderBottom: '1px solid #e2e8f0', display: 'flex', justifyContent: 'space-between', alignItems: 'center', position: 'sticky', top: 0, background: '#fff', zIndex: 10 }}>
              <h3 style={{ margin: 0, fontSize: '1.25rem', fontWeight: 700, color: '#1e293b' }}>
                {editingTour ? 'Chỉnh sửa Tour' : 'Tạo Tour Mới'}
              </h3>
              <button 
                onClick={() => setShowModal(false)}
                style={{ background: '#f1f5f9', border: 'none', width: '32px', height: '32px', borderRadius: '50%', display: 'flex', alignItems: 'center', justifyContent: 'center', cursor: 'pointer', color: '#64748b' }}
              >
                &times;
              </button>
            </div>
            <form onSubmit={handleSubmit} style={{ padding: '30px' }}>
              {/* Tabs Navigation */}
              <div style={{ display: 'flex', gap: '15px', marginBottom: '25px', borderBottom: '1px solid #e2e8f0', paddingBottom: '10px' }}>
                <button type="button" onClick={() => setActiveTab('general')} style={{ background: 'none', border: 'none', padding: '8px 12px', fontWeight: 600, cursor: 'pointer', color: activeTab === 'general' ? '#3b82f6' : '#64748b', borderBottom: activeTab === 'general' ? '2px solid #3b82f6' : '2px solid transparent', transition: 'all 0.2s' }}>
                  Thông tin chung
                </button>
                <button type="button" onClick={() => setActiveTab('images')} style={{ background: 'none', border: 'none', padding: '8px 12px', fontWeight: 600, cursor: 'pointer', color: activeTab === 'images' ? '#3b82f6' : '#64748b', borderBottom: activeTab === 'images' ? '2px solid #3b82f6' : '2px solid transparent', transition: 'all 0.2s' }}>
                  Hình ảnh
                </button>
                <button type="button" onClick={() => setActiveTab('details')} style={{ background: 'none', border: 'none', padding: '8px 12px', fontWeight: 600, cursor: 'pointer', color: activeTab === 'details' ? '#3b82f6' : '#64748b', borderBottom: activeTab === 'details' ? '2px solid #3b82f6' : '2px solid transparent', transition: 'all 0.2s' }}>
                  Lịch trình & Tiện ích
                </button>
              </div>

              {/* Tab 1: Thông tin chung */}
              {activeTab === 'general' && (
                <>
              <div style={{ marginBottom: '20px' }}>
                <label style={{ display: 'block', marginBottom: '8px', fontWeight: 600, color: '#475569', fontSize: '0.9rem' }}>Tên Tour <span style={{color: '#ef4444'}}>*</span></label>
                <input type="text" name="title" className="modern-input" placeholder="VD: Tour Đà Nẵng - Hội An" value={formData.title} onChange={handleInputChange} required />
              </div>
              
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: '20px', marginBottom: '20px' }}>
                <div>
                  <label style={{ display: 'block', marginBottom: '8px', fontWeight: 600, color: '#475569', fontSize: '0.9rem' }}>Giá (VNĐ) <span style={{color: '#ef4444'}}>*</span></label>
                  <input type="number" name="price" className="modern-input" value={formData.price} onChange={handleInputChange} required />
                </div>
                <div>
                  <label style={{ display: 'block', marginBottom: '8px', fontWeight: 600, color: '#475569', fontSize: '0.9rem' }}>Thời gian</label>
                  <input type="text" name="duration" className="modern-input" placeholder="VD: 3 Ngày 2 Đêm" value={formData.duration} onChange={handleInputChange} />
                </div>
                <div>
                  <label style={{ display: 'block', marginBottom: '8px', fontWeight: 600, color: '#475569', fontSize: '0.9rem' }}>Số lượng tối đa</label>
                  <input type="number" name="maxPeople" className="modern-input" min="1" value={formData.maxPeople} onChange={handleInputChange} />
                </div>
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '20px', marginBottom: '20px' }}>
                <div>
                  <label style={{ display: 'block', marginBottom: '12px', fontWeight: 600, color: '#475569', fontSize: '0.9rem', borderBottom: '1px solid #e2e8f0', paddingBottom: '8px' }}>Nơi lưu trú <span style={{color: '#ef4444'}}>*</span></label>
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '10px', maxHeight: '150px', overflowY: 'auto', padding: '10px', background: '#f8fafc', borderRadius: '8px', border: '1px solid #e2e8f0' }}>
                    {accommodations.map(acc => (
                      <label key={acc.id} style={{ display: 'flex', alignItems: 'center', gap: '8px', cursor: 'pointer' }}>
                        <input 
                          type="checkbox" 
                          checked={formData.accommodationIds.includes(acc.id)}
                          onChange={() => handleAccommodationToggle(acc.id)}
                          style={{ width: '16px', height: '16px', cursor: 'pointer' }}
                        />
                        <span style={{ fontSize: '0.9rem', color: '#334155' }}>{acc.name} - <span style={{ color: '#64748b' }}>{acc.address}</span></span>
                      </label>
                    ))}
                  </div>
                </div>
                <div>
                  <label style={{ display: 'block', marginBottom: '8px', fontWeight: 600, color: '#475569', fontSize: '0.9rem' }}>Điểm đến</label>
                  <input type="text" name="destination" className="modern-input" placeholder="VD: Đà Nẵng" value={formData.destination} onChange={handleInputChange} />
                </div>
              </div>

              <div style={{ marginBottom: '20px' }}>
                <label style={{ display: 'block', marginBottom: '8px', fontWeight: 600, color: '#475569', fontSize: '0.9rem' }}>Mô tả chi tiết</label>
                <textarea name="description" className="modern-input" rows={4} placeholder="Nhập mô tả về lịch trình, dịch vụ..." value={formData.description} onChange={handleInputChange}></textarea>
              </div>
                </>
              )}

              {/* Tab 2: Hình ảnh */}
              {activeTab === 'images' && (
                <>
              <div style={{ marginBottom: '20px' }}>
                <label style={{ display: 'block', marginBottom: '8px', fontWeight: 600, color: '#475569', fontSize: '0.9rem' }}>Ảnh bìa chính (Tải lên)</label>
                <div style={{ display: 'flex', gap: '10px', alignItems: 'center' }}>
                  <input type="file" accept="image/*" className="modern-input" onChange={handleCoverChange} disabled={uploadingCover} style={{ flex: 1, padding: '10px' }} />
                  {formData.imageUrl && (
                    <button type="button" onClick={() => setFormData(prev => ({...prev, imageUrl: ''}))} style={{ padding: '10px 16px', background: '#ef4444', color: 'white', border: 'none', borderRadius: '6px', cursor: 'pointer', fontWeight: 600 }}>Xóa ảnh</button>
                  )}
                </div>
                {uploadingCover && <p style={{fontSize: '0.9em', color: '#3b82f6', marginTop: '5px', fontWeight: 500}}>Đang tải ảnh bìa lên, vui lòng chờ...</p>}
                {formData.imageUrl && (
                  <div style={{ marginTop: '10px' }}>
                    <img src={formData.imageUrl} alt="Ảnh bìa" style={{ width: '120px', height: '120px', objectFit: 'cover', borderRadius: '8px', boxShadow: '0 2px 4px rgba(0,0,0,0.1)' }} />
                  </div>
                )}
              </div>

              <div style={{ background: '#f8fafc', padding: '20px', borderRadius: '12px', border: '1px dashed #cbd5e1' }}>
                <label style={{ display: 'block', marginBottom: '8px', fontWeight: 600, color: '#475569', fontSize: '0.9rem' }}>Thư viện ảnh Tour (Tải lên nhiều ảnh)</label>
                <input type="file" multiple accept="image/*" style={{ width: '100%', padding: '10px 0' }} onChange={handleFileChange} disabled={uploading} />
                {uploading && <p style={{fontSize: '0.9em', color: '#3b82f6', marginTop: '5px', fontWeight: 500}}>Đang tải ảnh lên Cloudinary, vui lòng chờ...</p>}
                
                {formData.images.length > 0 && (
                  <div style={{ display: 'flex', gap: '12px', marginTop: '16px', flexWrap: 'wrap' }}>
                    {formData.images.map((img, idx) => (
                      <div key={idx} style={{ position: 'relative', width: '80px', height: '80px', borderRadius: '8px', overflow: 'hidden', boxShadow: '0 2px 4px rgba(0,0,0,0.1)' }}>
                        <img src={img} alt={`tour-img-${idx}`} style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
                        <button 
                          type="button" 
                          onClick={() => removeImage(idx)}
                          style={{ position: 'absolute', top: '4px', right: '4px', background: 'rgba(239, 68, 68, 0.9)', color: 'white', border: 'none', borderRadius: '50%', width: '24px', height: '24px', cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '14px', transition: 'all 0.2s' }}
                        >
                          &times;
                        </button>
                      </div>
                    ))}
                  </div>
                )}
              </div>
                </>
              )}

              {/* Tab 3: Lịch trình & Tiện ích */}
              {activeTab === 'details' && (
                <>
                  <div style={{ marginBottom: '20px' }}>
                    <label style={{ display: 'block', marginBottom: '12px', fontWeight: 600, color: '#475569', fontSize: '1rem', borderBottom: '1px solid #e2e8f0', paddingBottom: '8px' }}>Tiện ích (Utilities)</label>
                    <div style={{ display: 'flex', flexWrap: 'wrap', gap: '15px' }}>
                      {utilities.map(u => (
                        <label key={u.id} style={{ display: 'flex', alignItems: 'center', gap: '6px', cursor: 'pointer' }}>
                          <input 
                            type="checkbox" 
                            checked={formData.utilityIds.includes(u.id)}
                            onChange={() => handleUtilityToggle(u.id)}
                            style={{ width: '18px', height: '18px', cursor: 'pointer' }}
                          />
                          <span>{u.name}</span>
                        </label>
                      ))}
                    </div>
                  </div>

                  <div style={{ marginBottom: '20px' }}>
                    <label style={{ display: 'block', marginBottom: '12px', fontWeight: 600, color: '#475569', fontSize: '1rem', borderBottom: '1px solid #e2e8f0', paddingBottom: '8px' }}>Điểm nổi bật (Highlights)</label>
                    {formData.highlights.map((hl, idx) => (
                      <div key={idx} style={{ display: 'flex', gap: '10px', marginBottom: '10px' }}>
                        <input type="text" className="modern-input" value={hl} onChange={(e) => handleHighlightChange(idx, e.target.value)} placeholder="Nhập điểm nổi bật..." style={{ flex: 1 }} />
                        <button type="button" onClick={() => handleRemoveHighlight(idx)} style={{ background: '#ef4444', color: 'white', border: 'none', borderRadius: '6px', padding: '0 15px', cursor: 'pointer', height: '42px' }}>Xóa</button>
                      </div>
                    ))}
                    <button type="button" onClick={handleAddHighlight} style={{ background: '#3b82f6', color: 'white', border: 'none', borderRadius: '6px', padding: '8px 16px', cursor: 'pointer', fontSize: '0.9rem', marginTop: '5px' }}>+ Thêm điểm nổi bật</button>
                  </div>

                  <div style={{ marginBottom: '20px' }}>
                    <label style={{ display: 'block', marginBottom: '12px', fontWeight: 600, color: '#475569', fontSize: '1rem', borderBottom: '1px solid #e2e8f0', paddingBottom: '8px' }}>Lịch trình chi tiết (Itinerary)</label>
                    {formData.itinerary.map((item, idx) => (
                      <div key={idx} style={{ background: '#f8fafc', padding: '15px', borderRadius: '8px', border: '1px solid #e2e8f0', marginBottom: '15px' }}>
                        <div style={{ display: 'flex', gap: '15px', marginBottom: '10px' }}>
                          <div style={{ flex: '0 0 120px' }}>
                            <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: 600, marginBottom: '5px' }}>Ngày</label>
                            <input type="text" className="modern-input" value={item.day} onChange={(e) => handleItineraryChange(idx, 'day', e.target.value)} placeholder="VD: Ngày 1" />
                          </div>
                          <div style={{ flex: 1 }}>
                            <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: 600, marginBottom: '5px' }}>Tiêu đề</label>
                            <input type="text" className="modern-input" value={item.title} onChange={(e) => handleItineraryChange(idx, 'title', e.target.value)} placeholder="VD: Tham quan Hà Nội" />
                          </div>
                          <button type="button" onClick={() => handleRemoveItinerary(idx)} style={{ background: '#ef4444', color: 'white', border: 'none', borderRadius: '6px', padding: '0 15px', cursor: 'pointer', height: '42px', alignSelf: 'flex-end' }}>Xóa</button>
                        </div>
                        <div>
                          <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: 600, marginBottom: '5px' }}>Mô tả</label>
                          <textarea className="modern-input" rows={2} value={item.description} onChange={(e) => handleItineraryChange(idx, 'description', e.target.value)} placeholder="Mô tả chi tiết hoạt động..."></textarea>
                        </div>
                      </div>
                    ))}
                    <button type="button" onClick={handleAddItinerary} style={{ background: '#3b82f6', color: 'white', border: 'none', borderRadius: '6px', padding: '8px 16px', cursor: 'pointer', fontSize: '0.9rem', marginTop: '5px' }}>+ Thêm ngày</button>
                  </div>
                </>
              )}

              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '12px', marginTop: '30px' }}>
                <button type="button" onClick={() => setShowModal(false)} style={{ padding: '12px 24px', borderRadius: '8px', border: '1px solid #cbd5e1', background: '#fff', color: '#475569', fontWeight: 600, cursor: 'pointer', transition: 'all 0.2s' }}>
                  Hủy bỏ
                </button>
                <button type="submit" className="btn-add-modern" style={{ padding: '12px 30px' }}>
                  {editingTour ? 'Lưu Thay Đổi' : 'Tạo Tour Mới'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default TourManagement;
