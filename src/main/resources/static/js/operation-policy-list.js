(function () {
    'use strict';

    // ========================================
    // Global State
    // ========================================
    let pendingToggleForm = null;

    // ========================================
    // DOM Ready
    // ========================================
    document.addEventListener('DOMContentLoaded', function () {
        initDeleteButtons();
        initRoomCountButtons();
        initToggleForms();
        initModalBackdrops();
        initKeyboardHandlers();
    });

    // ========================================
    // Delete Modal
    // ========================================
    function initDeleteButtons() {
        document.querySelectorAll('.action-btn-delete').forEach(function (btn) {
            btn.addEventListener('click', function (e) {
                e.preventDefault();
                openDeleteModal(this);
            });
        });
    }

    function openDeleteModal(element) {
        const policyId = element.dataset.policyId;
        const policyName = element.dataset.policyName;

        const modal = document.getElementById('deleteModal');
        const nameElement = document.getElementById('deletePolicyName');
        const form = document.getElementById('deleteForm');

        if (modal && nameElement && form) {
            nameElement.textContent = '"' + policyName + '"';
            form.action = '/admin/operation-policies/' + policyId + '/delete';
            showModal(modal);
        }
    }

    function closeDeleteModal() {
        const modal = document.getElementById('deleteModal');
        if (modal) {
            hideModal(modal);
        }
    }

    // ========================================
    // Toggle Status (Deactivation Confirmation)
    // ========================================
    function initToggleForms() {
        document.querySelectorAll('.toggle-form').forEach(function (form) {
            form.addEventListener('submit', function (e) {
                const isCurrentlyActive = form.dataset.currentActive === 'true';
                const hasRooms = form.dataset.hasRooms === 'true';
                const roomCount = parseInt(form.dataset.roomCount || '0', 10);

                // Only intercept deactivation when rooms are connected
                if (isCurrentlyActive && hasRooms) {
                    e.preventDefault();
                    pendingToggleForm = form;
                    openDeactivateBlockedModal(roomCount);
                }
                // Otherwise, let the form submit normally
            });
        });
    }

    function openDeactivateBlockedModal(roomCount) {
        const dialog = document.getElementById('toggleConfirmDialog');
        const message = document.getElementById('toggleConfirmMessage');

        if (dialog && message) {
            message.textContent = '현재 이 정책을 사용하는 룸이 ' + roomCount + '개 있습니다. 해당 룸들의 정책을 먼저 변경해야 비활성화할 수 있습니다.';
            showModal(dialog);
        }
    }

    function closeToggleConfirm() {
        const dialog = document.getElementById('toggleConfirmDialog');
        if (dialog) {
            hideModal(dialog);
            pendingToggleForm = null;
        }
    }

    // ========================================
    // Room List Modal
    // ========================================
    function initRoomCountButtons() {
        document.querySelectorAll('.room-count-link').forEach(function (btn) {
            btn.addEventListener('click', function (e) {
                e.preventDefault();
                openRoomModal(this);
            });
        });
    }

    function openRoomModal(element) {
        const policyId = element.dataset.policyId;

        const modal = document.getElementById('roomModal');
        const tableBody = document.getElementById('roomTableBody');

        if (modal && tableBody) {
            // Show loading state
            tableBody.innerHTML = '<tr><td colspan="4" class="table-message">로딩 중...</td></tr>';
            showModal(modal);

            // Fetch room data from API
            fetch('/api/admin/operation-policies/' + policyId + '/rooms')
                .then(function (response) {
                    if (!response.ok) throw new Error('Failed to fetch rooms');
                    return response.json();
                })
                .then(function (rooms) {
                    renderRoomTable(tableBody, rooms);
                })
                .catch(function (error) {
                    console.error('Error fetching rooms:', error);
                    tableBody.innerHTML = '<tr><td colspan="4" class="table-message table-message-error">룸 목록을 불러오는 데 실패했습니다.</td></tr>';
                });
        }
    }

    function renderRoomTable(tableBody, rooms) {
        tableBody.innerHTML = '';

        if (rooms.length === 0) {
            tableBody.innerHTML = '<tr><td colspan="4" class="table-message">이 정책을 사용하는 룸이 없습니다.</td></tr>';
            return;
        }

        rooms.forEach(function (room) {
            var row = document.createElement('tr');
            var statusClass = room.status === 'ACTIVE' ? 'badge-active' : 'badge-inactive';
            var statusText = room.status === 'ACTIVE' ? '활성' : '비활성';

            row.innerHTML =
                '<td class="text-primary">' + escapeHtml(String(room.id)) + '</td>' +
                '<td class="text-primary">' + escapeHtml(room.name) + '</td>' +
                '<td><span class="badge ' + statusClass + '">' + statusText + '</span></td>' +
                '<td>' +
                    '<a href="/admin/rooms/' + escapeHtml(String(room.id)) + '" class="link-detail">' +
                        '상세 보기' +
                        '<svg class="icon-xs" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">' +
                            '<path d="M18 13v6a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h6"></path>' +
                            '<polyline points="15 3 21 3 21 9"></polyline>' +
                            '<line x1="10" y1="14" x2="21" y2="3"></line>' +
                        '</svg>' +
                    '</a>' +
                '</td>';

            tableBody.appendChild(row);
        });
    }

    function closeRoomModal() {
        const modal = document.getElementById('roomModal');
        if (modal) {
            hideModal(modal);
        }
    }

    // ========================================
    // Modal Utilities
    // ========================================
    function showModal(modal) {
        modal.style.display = 'flex';
        document.body.style.overflow = 'hidden';
    }

    function hideModal(modal) {
        modal.style.display = 'none';
        document.body.style.overflow = '';
    }

    function initModalBackdrops() {
        // Delete modal backdrop
        var deleteBackdrop = document.querySelector('#deleteModal .modal-backdrop');
        if (deleteBackdrop) {
            deleteBackdrop.addEventListener('click', closeDeleteModal);
        }

        // Delete modal close button
        document.querySelectorAll('#deleteModal .modal-close, #deleteModal .btn-secondary').forEach(function (el) {
            el.addEventListener('click', function (e) {
                e.preventDefault();
                closeDeleteModal();
            });
        });

        // Toggle confirm dialog backdrop
        var toggleBackdrop = document.querySelector('#toggleConfirmDialog .modal-backdrop');
        if (toggleBackdrop) {
            toggleBackdrop.addEventListener('click', closeToggleConfirm);
        }

        // Toggle confirm dialog close button
        document.querySelectorAll('#toggleConfirmDialog .btn-primary').forEach(function (el) {
            el.addEventListener('click', function (e) {
                e.preventDefault();
                closeToggleConfirm();
            });
        });

        // Room modal backdrop
        var roomBackdrop = document.querySelector('#roomModal .modal-backdrop');
        if (roomBackdrop) {
            roomBackdrop.addEventListener('click', closeRoomModal);
        }

        // Room modal close buttons
        document.querySelectorAll('#roomModal .modal-close, #roomModal .btn-secondary').forEach(function (el) {
            el.addEventListener('click', function (e) {
                e.preventDefault();
                closeRoomModal();
            });
        });
    }

    // ========================================
    // Keyboard Handlers
    // ========================================
    function initKeyboardHandlers() {
        document.addEventListener('keydown', function (e) {
            if (e.key === 'Escape') {
                closeDeleteModal();
                closeToggleConfirm();
                closeRoomModal();
            }
        });
    }

    // ========================================
    // Utility Functions
    // ========================================
    function escapeHtml(text) {
        var div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }

    // ========================================
    // Expose functions for any remaining inline handlers
    // (Backwards compatibility during transition)
    // ========================================
    window.openDeleteModal = openDeleteModal;
    window.closeDeleteModal = closeDeleteModal;
    window.openRoomModal = openRoomModal;
    window.closeRoomModal = closeRoomModal;
    window.closeToggleConfirm = closeToggleConfirm;
    window.handleToggleSubmit = function (event, form) {
        // Legacy support - new code uses addEventListener
        const isCurrentlyActive = form.dataset.currentActive === 'true';
        const hasRooms = form.dataset.hasRooms === 'true';
        const roomCount = parseInt(form.dataset.roomCount || '0', 10);

        if (isCurrentlyActive && hasRooms) {
            event.preventDefault();
            pendingToggleForm = form;
            openDeactivateBlockedModal(roomCount);
            return false;
        }
        return true;
    };

})();
