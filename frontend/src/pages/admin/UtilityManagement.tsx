import React, { useState, useEffect } from 'react';
import { FaPlus, FaEdit, FaTrash, FaSearch } from 'react-icons/fa';
import Swal from 'sweetalert2';
import { UtilityService } from '../../services/UtilityService';
import '../../styles/admin.css';

interface Utility {
  id: number;
  name: string;
  description: string;
  isActive: boolean;
}

const UtilityManagement: React.FC = () => {
  const [utilities, setUtilities] = useState<Utility[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [searchTerm, setSearchTerm] = useState('');
  
  // Modal state
  const [showModal, setShowModal] = useState(false);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [formData, setFormData] = useState({
    name: '',
    description: '',
    isActive: true
  });
  const [errorMessage, setErrorMessage] = useState('');

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    try {
      setLoading(true);
      const data = await UtilityService.getAll();
      if (Array.isArray(data)) {
        setUtilities(data);
      }
    } catch (error) {
      console.error('Lỗi khi tải danh sách tiện ích:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { name, value, type } = e.target;
    if (type === 'checkbox') {
      const checked = (e.target as HTMLInputElement).checked;
      setFormData(prev => ({ ...prev, [name]: checked }));
    } else {
      setFormData(prev => ({ ...prev, [name]: value }));
    }
  };

  const openAddModal = () => {
    setEditingId(null);
    setFormData({ name: '', description: '', isActive: true });
    setErrorMessage('');
    setShowModal(true);
  };

  const openEditModal = (item: Utility) => {
    setEditingId(item.id);
    setFormData({
      name: item.name,
      description: item.description || '',
      isActive: item.isActive
    });
    setErrorMessage('');
    setShowModal(true);
  };

  const handleDelete = async (id: number) => {
    const result = await Swal.fire({
      title: 'Bạn có chắc chắn?',
      text: 'Bạn sẽ không thể khôi phục lại dữ liệu này!',
      icon: 'warning',
      showCancelButton: true,
      confirmButtonColor: '#d33',
      cancelButtonColor: '#3085d6',
      confirmButtonText: 'Xóa',
      cancelButtonText: 'Hủy'
    });

    if (result.isConfirmed) {
      try {
        await UtilityService.delete(id);
        Swal.fire('Thành công!', 'Xóa tiện ích thành công!', 'success');
        fetchData();
      } catch (error: any) {
        console.error('Lỗi khi xóa:', error);
        // Error from backend if used in Tour
        if (error.response && error.response.data && error.response.data.message) {
           Swal.fire('Lỗi', error.response.data.message, 'error');
        } else {
           Swal.fire('Lỗi', 'Không thể xóa tiện ích này do đang được sử dụng hoặc có lỗi hệ thống. Vui lòng chuyển trạng thái sang Không hoạt động.', 'error');
        }
      }
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      if (editingId) {
        await UtilityService.update(editingId, formData);
        Swal.fire('Thành công', 'Cập nhật thành công!', 'success');
      } else {
        await UtilityService.create(formData);
        Swal.fire('Thành công', 'Thêm mới thành công!', 'success');
      }
      setShowModal(false);
      fetchData();
    } catch (error: any) {
      console.error('Lỗi khi lưu dữ liệu:', error);
      setErrorMessage(error.response?.data?.message || 'Có lỗi xảy ra khi lưu dữ liệu');
    }
  };

  const filteredData = utilities.filter(item => 
    item.name.toLowerCase().includes(searchTerm.toLowerCase())
  );

  return (
    <div className="admin-page">
      <div className="admin-page-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
        <h2>Quản lý Tiện ích Tour</h2>
        <button className="btn-add-modern" onClick={openAddModal}>
          <FaPlus style={{ marginRight: '8px' }} /> Thêm Tiện ích
        </button>
      </div>

      <div className="admin-toolbar" style={{ marginBottom: '20px', display: 'flex', gap: '15px' }}>
        <div className="search-bar" style={{ flex: 1, position: 'relative' }}>
          <FaSearch style={{ position: 'absolute', left: '15px', top: '50%', transform: 'translateY(-50%)', color: '#94a3b8' }} />
          <input 
            type="text" 
            placeholder="Tìm kiếm theo tên tiện ích..." 
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            style={{ width: '100%', padding: '12px 15px 12px 40px', borderRadius: '8px', border: '1px solid #cbd5e1', outline: 'none' }}
          />
        </div>
      </div>

      <div className="admin-table-container">
        {loading ? (
          <p>Đang tải dữ liệu...</p>
        ) : (
          <table className="admin-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Tên Tiện ích</th>
                <th>Mô tả</th>
                <th>Trạng thái</th>
                <th>Thao tác</th>
              </tr>
            </thead>
            <tbody>
              {filteredData.length > 0 ? (
                filteredData.map(item => (
                  <tr key={item.id}>
                    <td>#{item.id}</td>
                    <td style={{ fontWeight: 'bold' }}>{item.name}</td>
                    <td>{item.description}</td>
                    <td>
                      <span style={{ 
                        padding: '4px 8px', 
                        borderRadius: '4px', 
                        fontSize: '0.85em', 
                        fontWeight: 'bold',
                        backgroundColor: item.isActive ? '#dcfce3' : '#fee2e2',
                        color: item.isActive ? '#166534' : '#991b1b'
                      }}>
                        {item.isActive ? 'Hoạt động' : 'Đã ẩn'}
                      </span>
                    </td>
                    <td>
                      <div className="action-buttons">
                        <button className="btn-edit" onClick={() => openEditModal(item)} title="Chỉnh sửa">
                          <FaEdit />
                        </button>
                        <button className="btn-delete" onClick={() => handleDelete(item.id)} title="Xóa">
                          <FaTrash />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan={5} style={{ textAlign: 'center', padding: '20px' }}>Không tìm thấy dữ liệu.</td>
                </tr>
              )}
            </tbody>
          </table>
        )}
      </div>

      {/* Modal */}
      {showModal && (
        <div className="modal-overlay" style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, backgroundColor: 'rgba(0,0,0,0.5)', display: 'flex', justifyContent: 'center', alignItems: 'center', zIndex: 1000 }}>
          <div className="modal-content" style={{ background: '#fff', borderRadius: '12px', width: '90%', maxWidth: '500px', maxHeight: '90vh', overflowY: 'auto' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '20px', borderBottom: '1px solid #e2e8f0' }}>
              <h3 style={{ margin: 0, fontSize: '1.25rem', color: '#1e293b' }}>{editingId ? 'Chỉnh sửa Tiện ích' : 'Thêm Tiện ích mới'}</h3>
              <button onClick={() => setShowModal(false)} style={{ background: 'none', border: 'none', fontSize: '1.5rem', cursor: 'pointer', color: '#64748b' }}>&times;</button>
            </div>
            
            <form onSubmit={handleSubmit} style={{ padding: '20px' }}>
              {errorMessage && <div style={{ color: '#ef4444', marginBottom: '15px', padding: '10px', background: '#fee2e2', borderRadius: '6px' }}>{errorMessage}</div>}
              
              <div style={{ marginBottom: '15px' }}>
                <label style={{ display: 'block', marginBottom: '8px', fontWeight: 600 }}>Tên Tiện ích *</label>
                <input type="text" name="name" className="modern-input" value={formData.name} onChange={handleInputChange} required placeholder="VD: Wi-Fi miễn phí" style={{ width: '100%', padding: '10px', borderRadius: '6px', border: '1px solid #cbd5e1' }} />
              </div>

              <div style={{ marginBottom: '15px' }}>
                <label style={{ display: 'block', marginBottom: '8px', fontWeight: 600 }}>Mô tả</label>
                <textarea name="description" className="modern-input" rows={4} value={formData.description} onChange={handleInputChange} placeholder="Mô tả thêm về tiện ích này..." style={{ width: '100%', padding: '10px', borderRadius: '6px', border: '1px solid #cbd5e1' }}></textarea>
              </div>

              <div style={{ marginBottom: '25px', display: 'flex', alignItems: 'center', gap: '10px' }}>
                <input type="checkbox" name="isActive" id="isActiveUtil" checked={formData.isActive} onChange={handleInputChange} style={{ width: '18px', height: '18px' }} />
                <label htmlFor="isActiveUtil" style={{ fontWeight: 600, cursor: 'pointer' }}>Đang hoạt động (Hiển thị khi tạo Tour)</label>
              </div>

              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '12px' }}>
                <button type="button" onClick={() => setShowModal(false)} style={{ padding: '10px 20px', borderRadius: '6px', border: '1px solid #cbd5e1', background: '#fff', cursor: 'pointer' }}>Hủy</button>
                <button type="submit" style={{ padding: '10px 20px', borderRadius: '6px', border: 'none', background: '#3b82f6', color: '#fff', cursor: 'pointer', fontWeight: 'bold' }}>{editingId ? 'Cập nhật' : 'Thêm mới'}</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default UtilityManagement;
