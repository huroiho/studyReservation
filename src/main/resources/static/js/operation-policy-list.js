/**
 * operation-policy-list.js
 * Vanilla JavaScript for Operation Policy List Page
 * Compatible with Spring Boot + Thymeleaf SSR
 */

// ========================================
// Global State
// ========================================
let pendingToggleForm = null;

// ========================================
// Delete Modal Functions
// ========================================
function openDeleteModal(element) {
    const policyId = element.dataset.policyId;
    const policyName = element.dataset.policyName;

    const modal = document.getElementById('deleteModal');
    const nameElement = document.getElementById('deletePolicyName');
    const form = document.getElementById('deleteForm');

    if (modal && nameElement && form) {
        nameElement.textContent = '"' + policyName + '"';
        form.action = '/admin/operation-policies/' + policyId + '/delete';

        modal.style.display = 'flex';
        document.body.style.overflow = 'hidden';
    }
}

function closeDeleteModal() {
    const modal = document.getElementById('deleteModal');
    if (modal) {
        modal.style.display = 'none';
        document.body.style.overflow = '';
    }
}

// ========================================
// Toggle Status Confirmation (Deactivation only)
// ========================================
function handleToggleSubmit(event, form) {
    const isCurrentlyActive = form.getAttribute('data-current-active') === 'true';
    const hasRooms = form.getAttribute('data-has-rooms') === 'true';
    const roomCount = parseInt(form.getAttribute('data-room-count') || '0');

    // Only intercept deactivation when rooms are connected
    if (isCurrentlyActive && hasRooms) {
        event.preventDefault();
        pendingToggleForm = form;
        openDeactivateBlockedModal(roomCount);
        return false;
    }

    // Otherwise, let the form submit normally
    return true;
}

function openDeactivateBlockedModal(roomCount) {
    const dialog = document.getElementById('toggleConfirmDialog');
    const message = document.getElementById('toggleConfirmMessage');

    if (dialog && message) {
        message.textContent = `현재 이 정책을 사용하는 룸이 ${roomCount}개 있습니다. 해당 룸들의 정책을 먼저 변경해야 비활성화할 수 있습니다.`;
        dialog.style.display = 'flex';
        document.body.style.overflow = 'hidden';
    }
}

function closeToggleConfirm() {
    const dialog = document.getElementById('toggleConfirmDialog');
    if (dialog) {
        dialog.style.display = 'none';
        document.body.style.overflow = '';
        pendingToggleForm = null;
    }
}

function confirmToggle() {
    // This is now just a "close and acknowledge" action since deactivation is blocked
    closeToggleConfirm();
}

// ========================================
// Room List Modal Functions
// ========================================
function openRoomModal(element) {
    const policyId = element.dataset.policyId;

    const modal = document.getElementById('roomModal');
    const tableBody = document.getElementById('roomTableBody');

    if (modal && tableBody) {
        tableBody.innerHTML = '<tr><td colspan="4" style="text-align: center; padding: 2rem;">로딩 중...</td></tr>';
        modal.style.display = 'flex';
        document.body.style.overflow = 'hidden';

        // Fetch room data from API
        fetch('/api/operation-policies/' + policyId + '/rooms')
            .then(response => {
                if (!response.ok) throw new Error('Failed to fetch rooms');
                return response.json();
            })
            .then(rooms => {
                tableBody.innerHTML = '';
                if (rooms.length === 0) {
                    tableBody.innerHTML = `
                        <tr>
                            <td colspan="4" style="text-align: center; padding: 2rem; color: var(--color-gray-600);">
                                이 정책을 사용하는 룸이 없습니다.
                            </td>
                        </tr>
                    `;
                } else {
                    rooms.forEach(room => {
                        const row = document.createElement('tr');
                        const statusKorean = room.status === 'ACTIVE' ? '활성' : '비활성';
                        row.innerHTML = `
                            <td class="text-primary">${room.id}</td>
                            <td class="text-primary">${room.name}</td>
                            <td>
                                <span class="badge ${room.status === 'ACTIVE' ? 'badge-active' : 'badge-inactive'}">
                                    ${statusKorean}
                                </span>
                            </td>
                            <td>
                                <a href="/admin/rooms/${room.id}" class="link-detail">
                                    상세 보기
                                    <svg class="icon-xs" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                        <path d="M18 13v6a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h6"></path>
                                        <polyline points="15 3 21 3 21 9"></polyline>
                                        <line x1="10" y1="14" x2="21" y2="3"></line>
                                    </svg>
                                </a>
                            </td>
                        `;
                        tableBody.appendChild(row);
                    });
                }
            })
            .catch(error => {
                console.error('Error fetching rooms:', error);
                tableBody.innerHTML = `
                    <tr>
                        <td colspan="4" style="text-align: center; padding: 2rem; color: #dc2626;">
                            룸 목록을 불러오는 데 실패했습니다.
                        </td>
                    </tr>
                `;
            });
    }
}

function closeRoomModal() {
    const modal = document.getElementById('roomModal');
    if (modal) {
        modal.style.display = 'none';
        document.body.style.overflow = '';
    }
}

// ========================================
// Badge Styles (for dynamically created content)
// ========================================
const badgeStyles = `
.badge {
    display: inline-flex;
    align-items: center;
    padding: 0.125rem 0.625rem;
    border-radius: 9999px;
    font-size: 0.75rem;
    font-weight: 500;
}
.badge-active {
    background-color: #dcfce7;
    color: #166534;
}
.badge-inactive {
    background-color: #f3f4f6;
    color: #4b5563;
}
.link-detail {
    display: inline-flex;
    align-items: center;
    font-size: 0.875rem;
    color: #2563eb;
    text-decoration: none;
    transition: color 0.15s ease;
}
.link-detail:hover {
    color: #1e40af;
    text-decoration: underline;
}
.link-detail .icon-xs {
    margin-left: 0.25rem;
    width: 0.75rem;
    height: 0.75rem;
}
`;

// ========================================
// Keyboard Event Handlers
// ========================================
document.addEventListener('DOMContentLoaded', function() {
    // Add inline styles for dynamically created content
    const styleElement = document.createElement('style');
    styleElement.textContent = badgeStyles;
    document.head.appendChild(styleElement);

    // Close modals on Escape key
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape') {
            closeDeleteModal();
            closeToggleConfirm();
            closeRoomModal();
        }
    });
});
