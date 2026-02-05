/**
 * Room Admin Create - JavaScript
 *
 * API Endpoints:
 * - POST /api/admin/rooms (multipart/form-data)
 * - PUT /api/admin/rooms/{id} (multipart/form-data)
 * - GET /api/admin/operation-policies/pick-items
 * - GET /api/admin/operation-policies/{id}
 * - GET /api/admin/room-rules/pick-items
 * - GET /api/admin/room-rules/{id}
 * - GET /api/admin/refund-policies/pick-items
 * - GET /api/admin/refund-policies/{id}
 */

// CSRF Token
const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;

// Policy Modal State
let currentPolicyType = null;
let selectedPolicyData = null;

// API Endpoints by Policy Type
const POLICY_CONFIG = {
    operationPolicy: {
        listUrl: '/api/admin/operation-policies/pick-items',
        detailUrl: (id) => `/api/admin/operation-policies/${id}`,
        title: '운영 정책 선택',
        inputId: 'operationPolicyId',
        summaryId: 'operationPolicySummary',
        renderDetail: renderOperationPolicyDetail
    },
    roomRule: {
        listUrl: '/api/admin/room-rules/pick-items',
        detailUrl: (id) => `/api/admin/room-rules/${id}`,
        title: '예약 규칙 선택',
        inputId: 'roomRuleId',
        summaryId: 'roomRuleSummary',
        renderDetail: renderRoomRuleDetail
    },
    refundPolicy: {
        listUrl: '/api/admin/refund-policies/pick-items',
        detailUrl: (id) => `/api/admin/refund-policies/${id}`,
        title: '환불 정책 선택',
        inputId: 'refundPolicyId',
        summaryId: 'refundPolicySummary',
        renderDetail: renderRefundPolicyDetail
    }
};

// ============ Initialize ============
document.addEventListener('DOMContentLoaded', () => {
    initializeForm();
    initializeImagePreviews();
});

function initializeForm() {
    const form = document.getElementById('roomCreateForm');
    form.addEventListener('submit', handleFormSubmit);
}

function initializeImagePreviews() {
    const mainImageInput = document.getElementById('mainImage');
    const generalImagesInput = document.getElementById('generalImages');

    mainImageInput.addEventListener('change', (e) => {
        previewMainImage(e.target.files[0]);
    });

    generalImagesInput.addEventListener('change', (e) => {
        previewGeneralImages(e.target.files);
    });
}

// ============ Image Preview ============
function previewMainImage(file) {
    const preview = document.getElementById('mainImagePreview');
    preview.innerHTML = '';

    if (!file) return;

    const reader = new FileReader();
    reader.onload = (e) => {
        preview.innerHTML = `
            <div class="preview-item">
                <img src="${e.target.result}" alt="대표 이미지 미리보기">
                <span class="preview-name">${file.name}</span>
            </div>
        `;
    };
    reader.readAsDataURL(file);
}

function previewGeneralImages(files) {
    const preview = document.getElementById('generalImagesPreview');
    preview.innerHTML = '';

    if (!files || files.length === 0) return;

    Array.from(files).slice(0, 10).forEach((file, index) => {
        const reader = new FileReader();
        reader.onload = (e) => {
            const div = document.createElement('div');
            div.className = 'preview-item';
            div.innerHTML = `
                <img src="${e.target.result}" alt="추가 이미지 ${index + 1}">
                <span class="preview-name">${file.name}</span>
            `;
            preview.appendChild(div);
        };
        reader.readAsDataURL(file);
    });
}

// ============ Policy Modal ============
async function openPolicyModal(policyType) {
    currentPolicyType = policyType;
    selectedPolicyData = null;

    const config = POLICY_CONFIG[policyType];
    const modal = document.getElementById('policyModal');

    // Reset modal state
    document.getElementById('modalTitle').textContent = config.title;
    document.getElementById('policyList').innerHTML = '';
    document.getElementById('policyListLoading').style.display = 'block';
    document.getElementById('policyListError').style.display = 'none';
    document.getElementById('policyDetailPlaceholder').style.display = 'flex';
    document.getElementById('policyDetailLoading').style.display = 'none';
    document.getElementById('policyDetailError').style.display = 'none';
    document.getElementById('policyDetailContent').style.display = 'none';
    document.getElementById('selectPolicyBtn').disabled = true;

    modal.style.display = 'flex';
    document.body.style.overflow = 'hidden';

    // Load policy list
    await loadPolicyList(config);
}

function closePolicyModal() {
    const modal = document.getElementById('policyModal');
    modal.style.display = 'none';
    document.body.style.overflow = '';
    currentPolicyType = null;
    selectedPolicyData = null;
}

async function loadPolicyList(config) {
    try {
        const response = await fetch(config.listUrl, {
            headers: {
                'Accept': 'application/json',
                ...(csrfHeader && csrfToken ? { [csrfHeader]: csrfToken } : {})
            }
        });

        if (!response.ok) {
            throw new Error('정책 목록을 불러오는데 실패했습니다.');
        }

        const result = await response.json();
        const policies = result.data || [];

        document.getElementById('policyListLoading').style.display = 'none';
        renderPolicyList(policies);
    } catch (error) {
        document.getElementById('policyListLoading').style.display = 'none';
        document.getElementById('policyListError').style.display = 'block';
        document.getElementById('policyListError').textContent = error.message;
    }
}

function renderPolicyList(policies) {
    const listEl = document.getElementById('policyList');

    if (policies.length === 0) {
        listEl.innerHTML = '<li class="empty-item">등록된 정책이 없습니다.</li>';
        return;
    }

    listEl.innerHTML = policies.map(policy => `
        <li class="policy-item" data-id="${policy.id}" data-name="${policy.name}" onclick="selectPolicyItem(this)">
            <span class="policy-id">#${policy.id}</span>
            <span class="policy-name">${policy.name}</span>
        </li>
    `).join('');
}

async function selectPolicyItem(element) {
    const policyId = element.dataset.id;
    const policyName = element.dataset.name;
    const config = POLICY_CONFIG[currentPolicyType];

    // Update selection UI
    document.querySelectorAll('.policy-item.selected').forEach(el => el.classList.remove('selected'));
    element.classList.add('selected');

    // Reset detail section
    document.getElementById('policyDetailPlaceholder').style.display = 'none';
    document.getElementById('policyDetailLoading').style.display = 'block';
    document.getElementById('policyDetailError').style.display = 'none';
    document.getElementById('policyDetailContent').style.display = 'none';

    try {
        const response = await fetch(config.detailUrl(policyId), {
            headers: {
                'Accept': 'application/json',
                ...(csrfHeader && csrfToken ? { [csrfHeader]: csrfToken } : {})
            }
        });

        if (!response.ok) {
            throw new Error('정책 상세 정보를 불러오는데 실패했습니다.');
        }

        const result = await response.json();
        const detail = result.data;

        document.getElementById('policyDetailLoading').style.display = 'none';
        document.getElementById('policyDetailContent').style.display = 'block';

        // Render detail based on policy type
        config.renderDetail(detail);

        // Enable select button
        selectedPolicyData = { id: policyId, name: policyName, detail: detail };
        document.getElementById('selectPolicyBtn').disabled = false;

    } catch (error) {
        document.getElementById('policyDetailLoading').style.display = 'none';
        document.getElementById('policyDetailError').style.display = 'block';
        document.getElementById('policyDetailError').textContent = error.message;
    }
}

// ============ Policy Detail Renderers ============
function renderOperationPolicyDetail(detail) {
    const container = document.getElementById('policyDetailContent');
    const dayNames = {
        'MONDAY': '월', 'TUESDAY': '화', 'WEDNESDAY': '수', 'THURSDAY': '목',
        'FRIDAY': '금', 'SATURDAY': '토', 'SUNDAY': '일'
    };

    const schedulesHtml = (detail.schedules || []).map(s => {
        const dayName = dayNames[s.dayOfWeek] || s.dayOfWeek;
        if (s.closed) {
            return `<tr><td>${dayName}</td><td class="text-muted">휴무</td></tr>`;
        }
        return `<tr><td>${dayName}</td><td>${formatTime(s.openTime)} ~ ${formatTime(s.closeTime)}</td></tr>`;
    }).join('');

    container.innerHTML = `
        <div class="detail-info">
            <div class="detail-row">
                <span class="detail-label">정책명</span>
                <span class="detail-value">${detail.name}</span>
            </div>
            <div class="detail-row">
                <span class="detail-label">슬롯 단위</span>
                <span class="detail-value">${formatSlotUnit(detail.slotUnit)}</span>
            </div>
            <div class="detail-row">
                <span class="detail-label">상태</span>
                <span class="detail-value ${detail.active ? 'status-active' : 'status-inactive'}">
                    ${detail.active ? '활성' : '비활성'}
                </span>
            </div>
        </div>
        <div class="detail-schedules">
            <h5>운영 시간</h5>
            <table class="schedule-table">
                <tbody>${schedulesHtml}</tbody>
            </table>
        </div>
    `;
}

function renderRoomRuleDetail(detail) {
    const container = document.getElementById('policyDetailContent');
    container.innerHTML = `
        <div class="detail-info">
            <div class="detail-row">
                <span class="detail-label">규칙명</span>
                <span class="detail-value">${detail.name}</span>
            </div>
            <div class="detail-row">
                <span class="detail-label">최소 이용 시간</span>
                <span class="detail-value">${detail.minDurationMinutes}분</span>
            </div>
            <div class="detail-row">
                <span class="detail-label">예약 가능 기간</span>
                <span class="detail-value">${detail.bookingOpenDays}일 전까지</span>
            </div>
            <div class="detail-row">
                <span class="detail-label">상태</span>
                <span class="detail-value ${detail.isActive ? 'status-active' : 'status-inactive'}">
                    ${detail.isActive ? '활성' : '비활성'}
                </span>
            </div>
            <div class="detail-row">
                <span class="detail-label">생성일</span>
                <span class="detail-value">${formatDateTime(detail.createdAt)}</span>
            </div>
        </div>
    `;
}

function renderRefundPolicyDetail(detail) {
    const container = document.getElementById('policyDetailContent');

    const rulesHtml = (detail.rules || []).map(rule => `
        <tr>
            <td>${rule.name}</td>
            <td>${rule.refundBaseMinutes}분 전</td>
            <td>${rule.refundRate}%</td>
        </tr>
    `).join('');

    container.innerHTML = `
        <div class="detail-info">
            <div class="detail-row">
                <span class="detail-label">정책명</span>
                <span class="detail-value">${detail.name}</span>
            </div>
            <div class="detail-row">
                <span class="detail-label">상태</span>
                <span class="detail-value ${detail.active ? 'status-active' : 'status-inactive'}">
                    ${detail.active ? '활성' : '비활성'}
                </span>
            </div>
            <div class="detail-row">
                <span class="detail-label">생성일</span>
                <span class="detail-value">${formatDateTime(detail.createdAt)}</span>
            </div>
        </div>
        <div class="detail-rules">
            <h5>환불 규칙</h5>
            <table class="rules-table">
                <thead>
                    <tr>
                        <th>규칙명</th>
                        <th>기준 시간</th>
                        <th>환불율</th>
                    </tr>
                </thead>
                <tbody>${rulesHtml || '<tr><td colspan="3" class="text-muted">등록된 규칙이 없습니다.</td></tr>'}</tbody>
            </table>
        </div>
    `;
}

function confirmPolicySelection() {
    if (!selectedPolicyData || !currentPolicyType) return;

    const config = POLICY_CONFIG[currentPolicyType];

    // Update hidden input
    document.getElementById(config.inputId).value = selectedPolicyData.id;

    // Update summary UI
    const summaryEl = document.getElementById(config.summaryId);
    summaryEl.classList.remove('empty');
    summaryEl.innerHTML = `
        <span class="selected-badge">
            <span class="selected-id">#${selectedPolicyData.id}</span>
            <span class="selected-name">${selectedPolicyData.name}</span>
        </span>
    `;

    closePolicyModal();
}

// ============ Form Submission ============
async function handleFormSubmit(event) {
    event.preventDefault();

    const form = event.target;
    const submitBtn = document.getElementById('submitBtn');
    const errorMessageEl = document.getElementById('errorMessage');

    const mode = form.dataset.mode || 'create';
    const roomId = form.dataset.roomId;
    const isEdit = mode === 'edit';

    // Validate policies are selected
    const operationPolicyId = document.getElementById('operationPolicyId').value;
    const roomRuleId = document.getElementById('roomRuleId').value;
    const refundPolicyId = document.getElementById('refundPolicyId').value;

    if (!operationPolicyId || !roomRuleId || !refundPolicyId) {
        showError('운영 정책, 예약 규칙, 환불 정책을 모두 선택해주세요.');
        return;
    }

    // Validate main image
    const mainImageInput = document.getElementById('mainImage');
    const hasMainImage = mainImageInput.files && mainImageInput.files.length > 0;
    if (!isEdit && !hasMainImage) {
        showError('대표 이미지를 선택해주세요.');
        return;
    }

    // Build FormData
    const formData = new FormData();
    formData.append('name', document.getElementById('name').value);
    formData.append('maxCapacity', document.getElementById('maxCapacity').value);
    formData.append('price', document.getElementById('price').value);
    formData.append('operationPolicyId', operationPolicyId);
    formData.append('roomRuleId', roomRuleId);
    formData.append('refundPolicyId', refundPolicyId);

    // Amenities (multiple values)
    document.querySelectorAll('input[name="amenities"]:checked').forEach(checkbox => {
        formData.append('amenities', checkbox.value);
    });

    // Main image
    if (hasMainImage) {
        formData.append('mainImage', mainImageInput.files[0]);
    }

    // General images
    const generalImagesInput = document.getElementById('generalImages');
    const hasGeneralImages = generalImagesInput.files && generalImagesInput.files.length > 0;
    if (hasGeneralImages) {
        Array.from(generalImagesInput.files).slice(0, 10).forEach(file => {
            formData.append('generalImages', file);
        });
    }

    // Show loading state
    submitBtn.disabled = true;
    submitBtn.querySelector('.btn-text').style.display = 'none';
    submitBtn.querySelector('.btn-loading').style.display = 'inline-flex';
    errorMessageEl.style.display = 'none';

    try {
        const url = isEdit ? `/api/admin/rooms/${roomId}` : '/api/admin/rooms';
        const method = isEdit ? 'PUT' : 'POST';

        const response = await fetch(url, {
            method,
            headers: {
                ...(csrfHeader && csrfToken ? { [csrfHeader]: csrfToken } : {})
            },
            body: formData
        });

        const result = await response.json();

        if (!response.ok) {
            throw new Error(result.message || (isEdit ? '룸 수정에 실패했습니다.' : '룸 등록에 실패했습니다.'));
        }

        // Success - redirect to room list
        window.location.href = '/admin/rooms';

    } catch (error) {
        showError(error.message);
        submitBtn.disabled = false;
        submitBtn.querySelector('.btn-text').style.display = 'inline';
        submitBtn.querySelector('.btn-loading').style.display = 'none';
    }
}

function showError(message) {
    const errorMessageEl = document.getElementById('errorMessage');
    errorMessageEl.textContent = message;
    errorMessageEl.style.display = 'block';
    errorMessageEl.scrollIntoView({ behavior: 'smooth', block: 'center' });
}

// ============ Utility Functions ============
function formatSlotUnit(slotUnit) {
    const labels = {
        'MINUTES_30': '30분',
        'MINUTES_60': '60분'
    };
    return labels[slotUnit] || slotUnit;
}

function formatTime(timeStr) {
    if (!timeStr) return '-';
    return timeStr.substring(0, 5); // "HH:mm:ss" -> "HH:mm"
}

function formatDateTime(dateTimeStr) {
    if (!dateTimeStr) return '-';
    const date = new Date(dateTimeStr);
    return date.toLocaleDateString('ko-KR', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit'
    });
}
