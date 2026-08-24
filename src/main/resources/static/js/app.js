/**
 * BookBasket — Natural, Modern & User-Friendly Book Rental & Library Platform
 * "Pick a book, find a world"
 */

const API_BASE = '/api';

const App = {
    state: {
        currentUser: null,
        books: [],
        categories: [],
        authors: [],
        borrowers: [],
        userLoans: [],
        activeGenreId: '',
        searchQuery: '',
        availableOnly: false,
        sortBy: 'borrowCount',
        currentTab: 'books'
    },

    // 20 Category Color Palettes for Unique Fallback Book Jackets
    categoryThemes: {
        1: { bg: 'linear-gradient(135deg, #065f46, #022c22)', icon: '📖', border: '#10b981' }, // Fiction
        2: { bg: 'linear-gradient(135deg, #1e293b, #0f172a)', icon: '🔍', border: '#64748b' }, // Mystery
        3: { bg: 'linear-gradient(135deg, #991b1b, #450a0a)', icon: '⚡', border: '#ef4444' }, // Thriller
        4: { bg: 'linear-gradient(135deg, #be185d, #700732)', icon: '💖', border: '#f43f5e' }, // Romance
        5: { bg: 'linear-gradient(135deg, #5b21b6, #2e1065)', icon: '✨', border: '#a855f7' }, // Fantasy
        6: { bg: 'linear-gradient(135deg, #0369a1, #082f49)', icon: '🚀', border: '#38bdf8' }, // Sci-Fi
        7: { bg: 'linear-gradient(135deg, #374151, #111827)', icon: '👻', border: '#9ca3af' }, // Horror
        8: { bg: 'linear-gradient(135deg, #78350f, #451a03)', icon: '🏛️', border: '#d97706' }, // Historical Fiction
        9: { bg: 'linear-gradient(135deg, #15803d, #14532d)', icon: '🧭', border: '#22c55e' }, // Adventure
        10: { bg: 'linear-gradient(135deg, #475569, #1e293b)', icon: '👤', border: '#94a3b8' }, // Biography
        11: { bg: 'linear-gradient(135deg, #b45309, #78350f)', icon: '✍️', border: '#f59e0b' }, // Autobiography
        12: { bg: 'linear-gradient(135deg, #047857, #064e3b)', icon: '🌱', border: '#34d399' }, // Self-Help
        13: { bg: 'linear-gradient(135deg, #6d28d9, #4c1d95)', icon: '🧠', border: '#c084fc' }, // Psychology
        14: { bg: 'linear-gradient(135deg, #854d0e, #533007)', icon: '📜', border: '#eab308' }, // Philosophy
        15: { bg: 'linear-gradient(135deg, #0f766e, #134e4a)', icon: '💼', border: '#14b8a6' }, // Business
        16: { bg: 'linear-gradient(135deg, #1e3a8a, #172554)', icon: '💻', border: '#3b82f6' }, // Technology
        17: { bg: 'linear-gradient(135deg, #a21caf, #701a75)', icon: '🎒', border: '#f0abfc' }, // Young Adult
        18: { bg: 'linear-gradient(135deg, #0284c7, #075985)', icon: '🎨', border: '#67e8f9' }, // Children's Literature
        19: { bg: 'linear-gradient(135deg, #9d174d, #500724)', icon: '✒️', border: '#fb7185' }, // Poetry
        20: { bg: 'linear-gradient(135deg, #92400e, #451a03)', icon: '👑', border: '#fbbf24' }  // Classics
    },

    // ================= INITIALIZATION =================
    async init() {
        console.log("🧺 Welcome to BookBasket: Pick a book, find a world! (100 Books, 20 Genres)");
        this.bindEvents();
        await this.loadInitialData();

        // Restore saved reader session
        const savedUserId = localStorage.getItem('bookbasket_user_id');
        if (savedUserId && this.state.borrowers.length > 0) {
            const found = this.state.borrowers.find(b => b.id == savedUserId);
            if (found) {
                this.setCurrentUser(found);
            }
        }
    },

    bindEvents() {
        // Header Search
        const searchInput = document.getElementById('header-search-input');
        let searchDebounce = null;
        searchInput?.addEventListener('input', (e) => {
            this.state.searchQuery = e.target.value;
            clearTimeout(searchDebounce);
            searchDebounce = setTimeout(() => this.filterAndRenderBooks(), 300);
        });

        // Drawer
        document.getElementById('btn-toggle-menu')?.addEventListener('click', () => this.toggleDrawer(true));

        // Global dropdown click outside
        document.addEventListener('click', (e) => {
            const popup = document.getElementById('profile-dropdown-popup');
            const pill = document.getElementById('nav-profile-pill');
            if (popup && pill && !pill.contains(e.target) && !popup.contains(e.target)) {
                popup.classList.remove('open');
            }
        });
    },

    // ================= NAVIGATION & VIEWS =================
    navigateTo(viewId) {
        document.querySelectorAll('.view-section').forEach(sec => sec.classList.remove('active'));
        const target = document.getElementById(`view-${viewId}`);
        if (target) {
            target.classList.add('active');
            window.scrollTo({ top: 0, behavior: 'smooth' });
        }

        // Highlight drawer item
        document.querySelectorAll('.drawer-item').forEach(item => {
            item.classList.remove('active');
        });

        if (viewId === 'home') this.filterAndRenderBooks();
        if (viewId === 'my-books') this.loadMyShelfView();
        if (viewId === 'insights') this.loadInsights();
    },

    toggleDrawer(open) {
        const drawer = document.getElementById('sidebar-drawer');
        const overlay = document.getElementById('drawer-overlay');
        if (open) {
            drawer?.classList.add('open');
            overlay?.classList.add('open');
        } else {
            drawer?.classList.remove('open');
            overlay?.classList.remove('open');
        }
    },

    scrollToSection(sectionId) {
        this.navigateTo('home');
        setTimeout(() => {
            const el = document.getElementById(sectionId);
            if (el) el.scrollIntoView({ behavior: 'smooth' });
        }, 100);
    },

    switchTab(tab) {
        this.state.currentTab = tab;
        const btnBooks = document.getElementById('tab-books');
        const btnAuthors = document.getElementById('tab-authors');
        const secTrending = document.getElementById('section-trending');
        const secAuthors = document.getElementById('section-authors');

        if (tab === 'books') {
            btnBooks?.classList.add('active');
            btnAuthors?.classList.remove('active');
            if (secTrending) secTrending.style.display = 'flex';
            if (secAuthors) secAuthors.style.display = 'none';
        } else {
            btnAuthors?.classList.add('active');
            btnBooks?.classList.remove('active');
            if (secTrending) secTrending.style.display = 'none';
            if (secAuthors) secAuthors.style.display = 'block';
            this.renderAuthorsGrid();
        }
    },

    // ================= INITIAL DATA FETCHING =================
    async loadInitialData() {
        try {
            await Promise.all([
                this.loadCategories(),
                this.loadAuthors(),
                this.loadBorrowers(),
                this.filterAndRenderBooks()
            ]);
        } catch (e) {
            console.error("Error loading initial data:", e);
        }
    },

    async loadCategories() {
        try {
            const res = await fetch(`${API_BASE}/categories`);
            const json = await res.json();
            if (json.success) {
                this.state.categories = json.data;
                const addSelect = document.getElementById('add-book-category');
                if (addSelect) {
                    addSelect.innerHTML = json.data.map(c => `<option value="${c.id}">${this.escapeHtml(c.name)}</option>`).join('');
                }
            }
        } catch (e) {
            console.error("Error loading categories:", e);
        }
    },

    async loadAuthors() {
        try {
            const res = await fetch(`${API_BASE}/authors`);
            const json = await res.json();
            if (json.success) {
                this.state.authors = json.data;
                const addSelect = document.getElementById('add-book-authors');
                if (addSelect) {
                    addSelect.innerHTML = json.data.map(a => `<option value="${a.id}">${this.escapeHtml(a.name)}</option>`).join('');
                }
                const badge = document.getElementById('authors-count-badge');
                if (badge) badge.textContent = json.data.length;
                this.updateAuthorUI();
            }
        } catch (e) {
            console.error("Error loading authors:", e);
        }
    },

    async loadBorrowers() {
        try {
            const res = await fetch(`${API_BASE}/borrowers`);
            const json = await res.json();
            if (json.success) {
                this.state.borrowers = json.data;
                const select = document.getElementById('select-login-reader');
                if (select) {
                    select.innerHTML = `
                        <option value="">-- Choose Account to Quick Fill --</option>
                        ${json.data.map(b => `
                            <option value="${b.id}">${this.escapeHtml(b.name)} (${this.escapeHtml(b.email)})</option>
                        `).join('')}
                    `;
                }
            }
        } catch (e) {
            console.error("Error loading borrowers:", e);
        }
    },

    // ================= AUTHENTICATION (EMAIL/PASSWORD + GOOGLE) =================
    openLoginModal() {
        document.getElementById('login-modal')?.classList.add('open');
    },

    toggleAuthMode(mode) {
        const tabLogin = document.getElementById('tab-login-btn');
        const tabReg = document.getElementById('tab-register-btn');
        const formLogin = document.getElementById('form-login');
        const formReg = document.getElementById('form-register');

        if (mode === 'login') {
            tabLogin?.classList.add('active');
            tabReg?.classList.remove('active');
            if (formLogin) formLogin.style.display = 'block';
            if (formReg) formReg.style.display = 'none';
        } else {
            tabReg?.classList.add('active');
            tabLogin?.classList.remove('active');
            if (formLogin) formLogin.style.display = 'none';
            if (formReg) formReg.style.display = 'block';
        }
    },

    handleGoogleLogin() {
        const defaultUser = this.state.borrowers.find(b => b.email.includes('likitha')) || this.state.borrowers[0];
        if (defaultUser) {
            this.setCurrentUser(defaultUser);
            this.closeModals();
            this.showToast('success', `Signed in with Google as ${defaultUser.name}! G`);
        }
    },

    async handleEmailLogin(e) {
        e.preventDefault();
        const email = document.getElementById('login-email')?.value.trim();
        const password = document.getElementById('login-password')?.value;

        if (!email || !password) {
            this.showToast('error', 'Please enter your email ID and password.');
            return;
        }

        try {
            const res = await fetch(`${API_BASE}/auth/login`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ email, password })
            });

            const json = await res.json();
            if (json.success && json.data) {
                await this.loadBorrowers();
                this.setCurrentUser(json.data);
                this.closeModals();
                this.showToast('success', `Welcome back, ${json.data.name}! 📖`);
            } else {
                this.showToast('error', json.message || 'Login failed.');
                if (json.message && json.message.toLowerCase().includes('sign up first')) {
                    this.toggleAuthMode('register');
                    const regEmail = document.getElementById('reg-email');
                    if (regEmail) regEmail.value = email;
                }
            }
        } catch (err) {
            this.showToast('error', 'Server connection error during login.');
        }
    },

    async handleRegisterSubmit(e) {
        e.preventDefault();
        const name = document.getElementById('reg-name')?.value.trim();
        const email = document.getElementById('reg-email')?.value.trim();
        const password = document.getElementById('reg-password')?.value;
        const phone = document.getElementById('reg-phone')?.value.trim();

        if (!name || !email || !password) {
            this.showToast('error', 'Please fill in all required fields.');
            return;
        }

        try {
            const res = await fetch(`${API_BASE}/auth/signup`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ name, email, password, phone })
            });

            const json = await res.json();
            if (json.success && json.data) {
                await this.loadBorrowers();
                this.setCurrentUser(json.data);
                this.closeModals();
                this.showToast('success', `Account created successfully! Welcome to BookBasket, ${json.data.name}! ✨`);
            } else {
                this.showToast('error', json.message || 'Signup failed.');
            }
        } catch (err) {
            this.showToast('error', 'Server error while creating account.');
        }
    },

    handleHeroAction() {
        if (this.state.currentUser) {
            this.scrollToSection('genres-section');
        } else {
            this.openLoginModal();
        }
    },

    getLoggedAuthorProfile() {
        if (!this.state.currentUser) return null;
        const saved = localStorage.getItem(`author_profile_user_${this.state.currentUser.id}`);
        if (saved) {
            try { return JSON.parse(saved); } catch (e) {}
        }
        // Fallback: match by user's full name in authors list
        const match = this.state.authors.find(a => a.name.toLowerCase() === this.state.currentUser.name.toLowerCase());
        if (match) {
            localStorage.setItem(`author_profile_user_${this.state.currentUser.id}`, JSON.stringify(match));
            return match;
        }
        return null;
    },

    isCurrentUserAuthor() {
        return !!this.getLoggedAuthorProfile();
    },

    updateAuthorUI() {
        const isAuthor = this.isCurrentUserAuthor();
        const authorProfile = this.getLoggedAuthorProfile();

        // 1. Desktop Nav Bar Slot
        const deskSlot = document.getElementById('desktop-author-slot');
        if (deskSlot) {
            if (isAuthor) {
                deskSlot.innerHTML = `<a href="#" class="nav-link" style="color: #047857; font-weight: 700;" onclick="App.openAddBookModal(); return false;">➕ Publish Book</a>`;
            } else {
                deskSlot.innerHTML = `<a href="#" class="nav-link" style="color: var(--primary-red); font-weight: 700;" onclick="App.handleAuthorNavClick(); return false;">✍️ Become an Author</a>`;
            }
        }

        // 2. Profile Dropdown Menu Buttons
        const btnProfileBecome = document.getElementById('btn-profile-become-author');
        const btnProfileAdd = document.getElementById('btn-profile-add-book');
        const roleTag = document.getElementById('nav-user-role-tag');

        if (btnProfileBecome) btnProfileBecome.style.display = isAuthor ? 'none' : 'block';
        if (btnProfileAdd) btnProfileAdd.style.display = isAuthor ? 'block' : 'none';
        if (roleTag) {
            roleTag.textContent = isAuthor ? '🌟 Verified Author' : '🟢 Active Reader';
            roleTag.style.color = isAuthor ? '#059669' : '';
        }

        // 3. Side Drawer Cards
        const drawerBecomeBox = document.getElementById('drawer-become-author-box');
        const drawerAddBox = document.getElementById('drawer-add-book-box');
        const drawerBadge = document.getElementById('drawer-author-name-badge');

        if (drawerBecomeBox) drawerBecomeBox.style.display = isAuthor ? 'none' : 'block';
        if (drawerAddBox) drawerAddBox.style.display = isAuthor ? 'block' : 'none';
        if (drawerBadge && authorProfile) drawerBadge.textContent = `Author: ${authorProfile.name}`;
    },

    handleAuthorNavClick() {
        if (!this.state.currentUser) {
            this.showToast('info', 'Please sign in first to become an author!');
            this.openLoginModal();
            return;
        }
        if (this.isCurrentUserAuthor()) {
            this.openAddBookModal();
        } else {
            this.openRegisterAuthorModal();
        }
    },

    openRegisterAuthorModal() {
        if (!this.state.currentUser) {
            this.showToast('info', 'Please sign in first to register as an Author!');
            this.openLoginModal();
            return;
        }

        const modal = document.getElementById('register-author-modal');
        const nameInp = document.getElementById('reg-author-name');
        if (nameInp && this.state.currentUser) {
            nameInp.value = this.state.currentUser.name;
        }
        modal?.classList.add('open');
    },

    async handleAuthorRegistrationSubmit(e) {
        e.preventDefault();
        if (!this.state.currentUser) {
            this.showToast('error', 'Please sign in first.');
            return;
        }

        const name = document.getElementById('reg-author-name')?.value.trim();
        const bio = document.getElementById('reg-author-bio')?.value.trim();
        const image = document.getElementById('reg-author-image')?.value.trim();

        if (!name || !bio) {
            this.showToast('error', 'Please provide author name and biography.');
            return;
        }

        try {
            const res = await fetch(`${API_BASE}/authors`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    name: name,
                    bio: bio,
                    imageUrl: image || null
                })
            });

            const json = await res.json();
            if (json.success && json.data) {
                // Save author profile locally
                localStorage.setItem(`author_profile_user_${this.state.currentUser.id}`, JSON.stringify(json.data));
                this.closeModals();
                this.showToast('success', `🎉 Congratulations, ${name}! You are now a registered Author! You can now publish books.`);
                
                await this.loadAuthors();
                this.updateAuthorUI();
                
                // Prompt user to immediately publish their first book
                setTimeout(() => {
                    this.openAddBookModal();
                }, 600);
            } else {
                this.showToast('error', json.message || 'Failed to register author.');
            }
        } catch (err) {
            this.showToast('error', 'Server error while registering author.');
        }
    },

    setCurrentUser(user) {
        this.state.currentUser = user;
        localStorage.setItem('bookbasket_user_id', user.id);

        const btnLogin = document.getElementById('btn-nav-login');
        const profilePill = document.getElementById('nav-profile-pill');
        const navName = document.getElementById('nav-user-name');
        const navAvatar = document.getElementById('nav-user-avatar');
        const dropName = document.getElementById('dropdown-user-name');
        const dropEmail = document.getElementById('dropdown-user-email');
        const heroActionBtn = document.getElementById('btn-hero-action');

        if (btnLogin) btnLogin.style.display = 'none';
        if (profilePill) profilePill.style.display = 'flex';
        if (navName) navName.textContent = user.name;
        if (navAvatar) navAvatar.textContent = user.name.split(' ').map(n => n[0]).join('').substring(0, 2).toUpperCase() || 'LN';
        if (dropName) dropName.textContent = user.name;
        if (dropEmail) dropEmail.textContent = user.email;

        // When user is signed in, hero button guides to explore books catalog
        if (heroActionBtn) {
            heroActionBtn.innerHTML = 'Explore Books ➔';
        }

        this.updateAuthorUI();
        this.loadUserShelf();
    },

    logout() {
        this.state.currentUser = null;
        localStorage.removeItem('bookbasket_user_id');

        const btnLogin = document.getElementById('btn-nav-login');
        const profilePill = document.getElementById('nav-profile-pill');
        const popup = document.getElementById('profile-dropdown-popup');
        const shelf = document.getElementById('user-shelf-section');
        const heroActionBtn = document.getElementById('btn-hero-action');

        if (btnLogin) btnLogin.style.display = 'flex';
        if (profilePill) profilePill.style.display = 'none';
        if (popup) popup.classList.remove('open');
        if (shelf) shelf.style.display = 'none';

        if (heroActionBtn) {
            heroActionBtn.innerHTML = 'Sign in to Read ➔';
        }

        this.updateAuthorUI();
        this.showToast('info', 'Logged out from BookBasket.');
    },

    toggleProfileDropdown(force) {
        const popup = document.getElementById('profile-dropdown-popup');
        if (!popup) return;
        if (typeof force === 'boolean') {
            if (force) popup.classList.add('open');
            else popup.classList.remove('open');
        } else {
            popup.classList.toggle('open');
        }
    },

    openSwitchAccountModal() {
        this.openLoginModal();
    },

    // ================= BORROWED SHELF =================
    async loadUserShelf() {
        if (!this.state.currentUser) return;
        const shelfSec = document.getElementById('user-shelf-section');
        const container = document.getElementById('user-shelf-cards-container');
        const badge = document.getElementById('user-shelf-badge');

        try {
            const res = await fetch(`${API_BASE}/borrowers/${this.state.currentUser.id}/history`);
            const json = await res.json();

            if (json.success) {
                this.state.userLoans = json.data;
                const active = json.data.filter(t => t.status === 'BORROWED' || t.status === 'OVERDUE');

                if (badge) badge.style.display = 'none';

                if (active.length > 0) {
                    if (shelfSec) shelfSec.style.display = 'block';
                    if (container) {
                        const userId = this.state.currentUser.id;
                        container.innerHTML = active.map(loan => {
                            const bookmark = localStorage.getItem(`bookmark_user_${userId}_book_${loan.bookId}`);
                            const highlights = JSON.parse(localStorage.getItem(`highlights_user_${userId}_book_${loan.bookId}`) || '[]');
                            return `
                                <div class="shelf-mini-card">
                                    <div class="book-cover-wrapper" style="width: 52px; height: 76px;">
                                        <img src="${loan.bookCoverImageUrl || this.getOpenLibraryCover(loan.bookId)}" alt="Cover" class="shelf-cover-img" onerror="App.handleImageFallback(this, '${this.escapeJsString(loan.bookTitle)}', '', 1)">
                                    </div>
                                    <div class="shelf-details">
                                        <h4 class="shelf-b-title" title="${this.escapeHtml(loan.bookTitle)}" onclick="App.openBookDetails(${loan.bookId})">${this.escapeHtml(loan.bookTitle)}</h4>
                                        <span class="shelf-b-due">Due: <strong>${loan.dueDate ? loan.dueDate.substring(0, 10) : ''}</strong></span>
                                        ${bookmark ? `<div style="font-size: 0.6875rem; color: #b45309; font-weight: 700; margin-top: 2px;">🔖 ${this.escapeHtml(bookmark)}</div>` : ''}
                                        ${highlights.length > 0 ? `<div style="font-size: 0.6875rem; color: #047857; font-weight: 600;">🖍️ ${highlights.length} saved highlights</div>` : ''}
                                        <div class="shelf-btn-row" style="display: flex; gap: 4px; margin-top: 6px; flex-wrap: wrap;">
                                            <button class="btn-sm-read" style="padding: 3px 8px; font-size: 0.75rem;" onclick="App.openReadModal(${loan.bookId}, '${this.escapeJsString(loan.bookTitle)}')">📖 Read & Edit</button>
                                            <button class="btn-sm-return" style="background: #ef4444; color: white; border: none; padding: 3px 8px; font-size: 0.75rem; border-radius: 4px; cursor: pointer;" title="Delete from my shelf" onclick="App.deleteFromShelf(${loan.bookId}, '${this.escapeJsString(loan.bookTitle)}')">🗑️ Remove</button>
                                        </div>
                                    </div>
                                </div>
                            `;
                        }).join('');
                    }
                } else {
                    if (shelfSec) shelfSec.style.display = 'none';
                }
            }
        } catch (e) {
            console.error("Error loading shelf:", e);
        }
    },

    scrollShelf(amount) {
        document.getElementById('user-shelf-cards-container')?.scrollBy({ left: amount, behavior: 'smooth' });
    },

    scrollGenres(amount) {
        document.getElementById('genres-icons-track')?.scrollBy({ left: amount, behavior: 'smooth' });
    },

    // ================= GENRE FILTERING =================
    async selectGenre(catId) {
        this.state.activeGenreId = catId ? String(catId) : '';
        document.querySelectorAll('#genres-icons-track .genre-bubble').forEach(b => {
            const id = b.getAttribute('data-cat-id');
            if (id === this.state.activeGenreId) b.classList.add('active');
            else b.classList.remove('active');
        });

        // Ensure we are viewing books tab
        if (this.state.currentTab !== 'books') {
            this.switchTab('books');
        }

        // Clear header search if filtering by explicit genre
        const searchInput = document.getElementById('header-search-input');
        if (searchInput && this.state.searchQuery) {
            searchInput.value = '';
            this.state.searchQuery = '';
        }

        await this.filterAndRenderBooks();

        // Smooth scroll to books grid for instant visual feedback
        const booksSec = document.getElementById('section-trending');
        if (booksSec) {
            booksSec.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
        }
    },

    handleAvailabilityFilter(checked) {
        this.state.availableOnly = checked;
        this.filterAndRenderBooks();
    },

    handleSortChange(val) {
        this.state.sortBy = val;
        this.filterAndRenderBooks();
    },

    triggerSearch() {
        const query = document.getElementById('header-search-input')?.value.trim();
        this.state.searchQuery = query;
        this.filterAndRenderBooks();
    },

    // ================= BOOKS CATALOG & GRID RENDERING =================
    async filterAndRenderBooks() {
        const container = document.getElementById('books-grid-main');
        const badge = document.getElementById('books-count-badge');
        const heading = document.getElementById('books-section-heading');

        if (container) {
            container.innerHTML = `
                <div class="loading-catalog-box" style="grid-column: 1/-1; text-align: center; padding: 40px;">
                    <div class="spinner-book"></div>
                    <p style="color: var(--text-muted); margin-top: 10px;">Finding books in BookBasket catalog...</p>
                </div>
            `;
        }

        try {
            const params = new URLSearchParams();
            if (this.state.searchQuery) params.append('query', this.state.searchQuery);
            if (this.state.activeGenreId) params.append('categoryId', this.state.activeGenreId);
            if (this.state.availableOnly) params.append('availableOnly', 'true');
            if (this.state.sortBy) params.append('sortBy', this.state.sortBy);

            // Calls GET /api/books/search
            const res = await fetch(`${API_BASE}/books/search?${params.toString()}`);
            const json = await res.json();

            if (json.success && container) {
                this.state.books = json.data;
                if (badge) badge.style.display = 'none';
                if (heading) {
                    if (this.state.activeGenreId) {
                        const cat = this.state.categories.find(c => c.id == this.state.activeGenreId);
                        heading.textContent = cat ? `${cat.name}` : 'Filtered Collection';
                    } else if (this.state.searchQuery) {
                        heading.textContent = `Search Results for "${this.state.searchQuery}"`;
                    } else {
                        heading.textContent = 'Books & Best Sellers';
                    }
                }
                this.renderBooks(json.data);
            }
        } catch (e) {
            console.error("Error searching books:", e);
        }
    },

    renderBooks(books) {
        const container = document.getElementById('books-grid-main');
        if (!container) return;

        if (!books || books.length === 0) {
            container.innerHTML = `
                <div style="grid-column: 1/-1; text-align: center; padding: 60px 20px; background: rgba(255,255,255,0.9); border-radius: var(--radius-md); border: 1px solid var(--border-light);">
                    <span style="font-size: 2.5rem;">🔍</span>
                    <h3 style="margin-top: 8px; color: var(--text-main);">No Books Found</h3>
                    <p style="color: var(--text-muted); font-size: 0.875rem;">Try searching for another title, author, or selecting a different genre.</p>
                </div>
            `;
            return;
        }

        container.innerHTML = books.map(book => {
            const authors = book.authors ? book.authors.map(a => a.name).join(', ') : 'Unknown Author';
            const catName = book.category ? book.category.name : 'General';
            const catId = book.category ? book.category.id : 1;
            const available = book.availableCopies;
            const total = book.totalCopies;
            const coverUrl = book.coverImageUrl || `https://covers.openlibrary.org/b/isbn/${book.isbn}-L.jpg`;

            return `
                <div class="user-book-card">
                    <!-- Unique Cover Image Wrapper with Automatic Genre Fallback -->
                    <div class="book-cover-wrapper" onclick="App.openBookDetails(${book.id})">
                        <img src="${coverUrl}" alt="${this.escapeHtml(book.title)}" class="book-cover-img" onerror="App.handleImageFallback(this, '${this.escapeJsString(book.title)}', '${this.escapeJsString(authors)}', ${catId})">
                    </div>

                    <!-- Book Information Column -->
                    <div class="book-card-right-details">
                        <div>
                            <span class="book-genre-pill">${this.escapeHtml(catName)}</span>
                            <h4 class="book-title-h4" title="${this.escapeHtml(book.title)}" onclick="App.openBookDetails(${book.id})">
                                ${this.escapeHtml(book.title)}
                            </h4>
                            <div class="book-author-line">${this.escapeHtml(authors)}</div>
                            <div style="font-size: 0.6875rem; color: var(--text-subtle); margin-top: 2px;">ISBN: ${this.escapeHtml(book.isbn)}</div>
                        </div>

                        <div>
                            <div class="book-stock-indicator ${available > 0 ? 'in-stock' : 'out-of-stock'}">
                                ${available > 0 ? `🟢 Available (${available} / ${total})` : '🔴 Out of Stock'}
                            </div>
                            <div style="display: flex; gap: 6px; align-items: center; margin-top: 6px; flex-wrap: wrap;">
                                <button class="btn-card-rent" style="margin-top: 0;" onclick="App.borrowBook(${book.id}, '${this.escapeJsString(book.title)}', ${available})" ${available === 0 ? 'disabled' : ''}>
                                    ${available === 0 ? 'Unavailable' : 'Borrow'}
                                </button>
                                <button class="btn-action-outline red" style="padding: 4px 10px; font-size: 0.75rem; margin-top: 0;" onclick="App.openBookDetails(${book.id})">
                                    View Details
                                </button>
                                <button class="btn-sm-read" style="padding: 4px 8px; font-size: 0.75rem; margin-top: 0;" onclick="App.openReadModal(${book.id}, '${this.escapeJsString(book.title)}')">
                                    Read
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            `;
        }).join('');
    },

    handleImageFallback(imgEl, title, author, catId) {
        const theme = this.categoryThemes[catId] || this.categoryThemes[1];
        const wrapper = imgEl.parentElement;
        if (wrapper) {
            wrapper.innerHTML = `
                <div class="dynamic-book-jacket" style="background: ${theme.bg};">
                    <span style="font-size: 1.1rem;">${theme.icon}</span>
                    <strong class="jacket-title">${this.escapeHtml(title)}</strong>
                    <span class="jacket-author">${this.escapeHtml(author)}</span>
                </div>
            `;
        }
    },

    getOpenLibraryCover(bookId) {
        const b = this.state.books.find(x => x.id === bookId);
        return b?.isbn ? `https://covers.openlibrary.org/b/isbn/${b.isbn}-L.jpg` : 'images/vintage-books.jpg';
    },

    // ================= AUTHORS GRID & WRITTEN BOOKS =================
    authorPhotos: {
        'J. K. Rowling': 'https://upload.wikimedia.org/wikipedia/commons/thumb/5/5d/J._K._Rowling_2010.jpg/440px-J._K._Rowling_2010.jpg',
        'George Orwell': 'https://upload.wikimedia.org/wikipedia/commons/thumb/7/7e/George_Orwell_press_photo.jpg/440px-George_Orwell_press_photo.jpg',
        'Paulo Coelho': 'https://upload.wikimedia.org/wikipedia/commons/thumb/0/0b/Paulo_Coelho_nr統一.jpg/440px-Paulo_Coelho_nr統一.jpg',
        'Harper Lee': 'https://upload.wikimedia.org/wikipedia/commons/thumb/f/f6/HarperLee_May1960.jpg/440px-HarperLee_May1960.jpg',
        'F. Scott Fitzgerald': 'https://upload.wikimedia.org/wikipedia/commons/thumb/5/5c/F_Scott_Fitzgerald_1921.jpg/440px-F_Scott_Fitzgerald_1921.jpg',
        'Jane Austen': 'https://upload.wikimedia.org/wikipedia/commons/thumb/c/cc/CassandraAusten-JaneAusten%28c.1810%29_hirez.jpg/440px-CassandraAusten-JaneAusten%28c.1810%29_hirez.jpg',
        'Charlotte Brontë': 'https://upload.wikimedia.org/wikipedia/commons/thumb/8/8d/Charlotte_Bront%C3%AB_by_George_Richmond.jpg/440px-Charlotte_Bront%C3%AB_by_George_Richmond.jpg',
        'Emily Brontë': 'https://upload.wikimedia.org/wikipedia/commons/thumb/4/4f/Emily_Bront%C3%AB_by_Patrick_Branwell_Bront%C3%AB_restored.jpg/440px-Emily_Bront%C3%AB_by_Patrick_Branwell_Bront%C3%AB_restored.jpg',
        'Louisa May Alcott': 'https://upload.wikimedia.org/wikipedia/commons/thumb/8/82/Louisa_May_Alcott%2C_c._1870_-_4791244410.jpg/440px-Louisa_May_Alcott%2C_c._1870_-_4791244410.jpg',
        'J. D. Salinger': 'https://upload.wikimedia.org/wikipedia/commons/thumb/8/8c/JD_Salinger.jpg/440px-JD_Salinger.jpg',
        'Khaled Hosseini': 'https://upload.wikimedia.org/wikipedia/commons/thumb/5/55/Khaled_Hosseini_2007_%28cropped%29.jpg/440px-Khaled_Hosseini_2007_%28cropped%29.jpg',
        'Markus Zusak': 'https://upload.wikimedia.org/wikipedia/commons/thumb/7/77/Markus_Zusak_2011.jpg/440px-Markus_Zusak_2011.jpg',
        'Yann Martel': 'https://upload.wikimedia.org/wikipedia/commons/thumb/e/e0/Yann_Martel_at_the_Eden_Mills_Writers%27_Festival.jpg/440px-Yann_Martel_at_the_Eden_Mills_Writers%27_Festival.jpg',
        'Delia Owens': 'https://upload.wikimedia.org/wikipedia/commons/thumb/9/91/Delia_Owens_2019.jpg/440px-Delia_Owens_2019.jpg',
        'Dan Brown': 'https://upload.wikimedia.org/wikipedia/commons/thumb/2/28/Dan_Brown_book_signing.jpg/440px-Dan_Brown_book_signing.jpg',
        'Alex Michaelides': 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=150',
        'Gillian Flynn': 'https://upload.wikimedia.org/wikipedia/commons/thumb/4/4e/Gillian_Flynn_2014.jpg/440px-Gillian_Flynn_2014.jpg',
        'Paula Hawkins': 'https://upload.wikimedia.org/wikipedia/commons/thumb/5/50/Paula_Hawkins_in_2015.jpg/440px-Paula_Hawkins_in_2015.jpg',
        'Colleen Hoover': 'https://upload.wikimedia.org/wikipedia/commons/thumb/9/99/Colleen_Hoover_2022.jpg/440px-Colleen_Hoover_2022.jpg',
        'John Green': 'https://upload.wikimedia.org/wikipedia/commons/thumb/6/6e/John_Green_2019_by_Glenn_Francis.jpg/440px-John_Green_2019_by_Glenn_Francis.jpg',
        'Jojo Moyes': 'https://upload.wikimedia.org/wikipedia/commons/thumb/f/f6/Jojo_Moyes_%283437340156%29.jpg/440px-Jojo_Moyes_%283437340156%29.jpg',
        'Nicholas Sparks': 'https://upload.wikimedia.org/wikipedia/commons/thumb/1/1a/Nicholas_Sparks_2014.jpg/440px-Nicholas_Sparks_2014.jpg',
        'J. R. R. Tolkien': 'https://upload.wikimedia.org/wikipedia/commons/thumb/d/d4/J._R._R._Tolkien%2C_ca._1925.jpg/440px-J._R._R._Tolkien%2C_ca._1925.jpg',
        'George R. R. Martin': 'https://upload.wikimedia.org/wikipedia/commons/thumb/e/ed/Portrait_photoshoot_at_Worldcon_75%2C_Helsinki%2C_before_the_Hugo_Awards_%E2%80%93_George_R._R._Martin.jpg/440px-Portrait_photoshoot_at_Worldcon_75%2C_Helsinki%2C_before_the_Hugo_Awards_%E2%80%93_George_R._R._Martin.jpg',
        'Patrick Rothfuss': 'https://upload.wikimedia.org/wikipedia/commons/thumb/6/6a/Patrick_Rothfuss_Lucca_Comics_2014.JPG/440px-Patrick_Rothfuss_Lucca_Comics_2014.JPG',
        'C. S. Lewis': 'https://upload.wikimedia.org/wikipedia/commons/thumb/1/1e/C.s.lewis3.JPG/440px-C.s.lewis3.JPG',
        'Frank Herbert': 'https://upload.wikimedia.org/wikipedia/commons/thumb/8/8e/Frank_Herbert_-_1984.jpg/440px-Frank_Herbert_-_1984.jpg',
        'Isaac Asimov': 'https://upload.wikimedia.org/wikipedia/commons/thumb/3/34/Isaac.Asimov01.jpg/440px-Isaac.Asimov01.jpg',
        'Ray Bradbury': 'https://upload.wikimedia.org/wikipedia/commons/thumb/c/c2/Ray_Bradbury_1975_portrait_%28cropped%29.jpg/440px-Ray_Bradbury_1975_portrait_%28cropped%29.jpg',
        'Aldous Huxley': 'https://upload.wikimedia.org/wikipedia/commons/thumb/8/88/Aldous_Huxley_psychical_researcher.jpg/440px-Aldous_Huxley_psychical_researcher.jpg',
        'Andy Weir': 'https://upload.wikimedia.org/wikipedia/commons/thumb/7/74/Andy_Weir_2015_%28cropped%29.jpg/440px-Andy_Weir_2015_%28cropped%29.jpg',
        'Stephen King': 'https://upload.wikimedia.org/wikipedia/commons/thumb/e/e3/Stephen_King%2C_Comicon.jpg/440px-Stephen_King%2C_Comicon.jpg',
        'Bram Stoker': 'https://upload.wikimedia.org/wikipedia/commons/thumb/3/34/Bram_Stoker_1906.jpg/440px-Bram_Stoker_1906.jpg',
        'Mary Shelley': 'https://upload.wikimedia.org/wikipedia/commons/thumb/6/65/RothwellMaryShelley.jpg/440px-RothwellMaryShelley.jpg',
        'Shirley Jackson': 'https://upload.wikimedia.org/wikipedia/commons/thumb/e/e8/Shirley_Jackson.jpg/440px-Shirley_Jackson.jpg',
        'Walter Isaacson': 'https://upload.wikimedia.org/wikipedia/commons/thumb/7/77/Walter_Isaacson_2015.jpg/440px-Walter_Isaacson_2015.jpg',
        'Michelle Obama': 'https://upload.wikimedia.org/wikipedia/commons/thumb/4/4b/Michelle_Obama_2013_official_portrait.jpg/440px-Michelle_Obama_2013_official_portrait.jpg',
        'Tara Westover': 'https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=150',
        'Nelson Mandela': 'https://upload.wikimedia.org/wikipedia/commons/thumb/0/02/Nelson_Mandela_1994.jpg/440px-Nelson_Mandela_1994.jpg',
        'A. P. J. Abdul Kalam': 'https://upload.wikimedia.org/wikipedia/commons/thumb/b/b0/A._P._J._Abdul_Kalam_in_2008.jpg/440px-A._P._J._Abdul_Kalam_in_2008.jpg',
        'James Clear': 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150',
        'Stephen R. Covey': 'https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=150',
        'Dale Carnegie': 'https://upload.wikimedia.org/wikipedia/commons/thumb/a/ae/Dale_Carnegie.jpg/440px-Dale_Carnegie.jpg',
        'Eckhart Tolle': 'https://upload.wikimedia.org/wikipedia/commons/thumb/8/87/Eckhart_Tolle_september_2008.jpg/440px-Eckhart_Tolle_september_2008.jpg',
        'Viktor E. Frankl': 'https://upload.wikimedia.org/wikipedia/commons/thumb/7/72/Viktor_Frankl2.jpg/440px-Viktor_Frankl2.jpg',
        'Daniel Kahneman': 'https://upload.wikimedia.org/wikipedia/commons/thumb/a/a2/Daniel_Kahneman_%28Nobel_2002%29.jpg/440px-Daniel_Kahneman_%28Nobel_2002%29.jpg',
        'Sun Tzu': 'https://upload.wikimedia.org/wikipedia/commons/thumb/8/8a/Sun_Tzu.jpg/440px-Sun_Tzu.jpg',
        'Marcus Aurelius': 'https://upload.wikimedia.org/wikipedia/commons/thumb/9/91/Marcus_Aurelius_Glyptothek_Munich.jpg/440px-Marcus_Aurelius_Glyptothek_Munich.jpg',
        'Plato': 'https://upload.wikimedia.org/wikipedia/commons/thumb/8/88/Plato_Silanion_Musei_Capitolini_MC1377.jpg/440px-Plato_Silanion_Musei_Capitolini_MC1377.jpg',
        'Robert Kiyosaki': 'https://images.unsplash.com/photo-1519085360753-af0119f7cbe7?w=150',
        'Morgan Housel': 'https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=150',
        'Benjamin Graham': 'https://upload.wikimedia.org/wikipedia/commons/thumb/3/30/Benjamin_Graham_%281894-1976%29_portrait%2C_c._1930s.jpg/440px-Benjamin_Graham_%281894-1976%29_portrait%2C_c._1930s.jpg',
        'Peter Thiel': 'https://upload.wikimedia.org/wikipedia/commons/thumb/2/2b/Peter_Thiel_by_Gage_Skidmore.jpg/440px-Peter_Thiel_by_Gage_Skidmore.jpg',
        'Robert C. Martin': 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150',
        'Antoine de Saint-Exupéry': 'https://upload.wikimedia.org/wikipedia/commons/thumb/7/7b/Antoine_de_Saint-Exup%C3%A9ry.jpg/440px-Antoine_de_Saint-Exup%C3%A9ry.jpg',
        'Roald Dahl': 'https://upload.wikimedia.org/wikipedia/commons/thumb/b/b3/Roald_Dahl_1954.jpg/440px-Roald_Dahl_1954.jpg',
        'L. Frank Baum': 'https://upload.wikimedia.org/wikipedia/commons/thumb/7/79/Lyman_Frank_Baum_1911.jpg/440px-Lyman_Frank_Baum_1911.jpg',
        'Walt Whitman': 'https://upload.wikimedia.org/wikipedia/commons/thumb/4/4e/Walt_Whitman_-_1860s_crop.jpg/440px-Walt_Whitman_-_1860s_crop.jpg',
        'Ernest Hemingway': 'https://upload.wikimedia.org/wikipedia/commons/thumb/2/28/ErnestHemingway.jpg/440px-ErnestHemingway.jpg'
    },

    getAuthorPhoto(name) {
        if (this.authorPhotos[name]) return this.authorPhotos[name];
        return `https://ui-avatars.com/api/?name=${encodeURIComponent(name)}&background=f97316&color=fff&size=150&font-size=0.4`;
    },

    showAuthorsList() {
        const listView = document.getElementById('authors-list-view');
        const spotView = document.getElementById('author-spotlight-view');
        if (listView) listView.style.display = 'block';
        if (spotView) spotView.style.display = 'none';
        window.scrollTo({ top: 400, behavior: 'smooth' });
    },

    renderAuthorsGrid() {
        const container = document.getElementById('authors-grid-container');
        if (!container) return;

        this.showAuthorsList();

        container.innerHTML = this.state.authors.map(a => {
            const photoUrl = this.getAuthorPhoto(a.name);
            return `
                <div class="author-profile-card" onclick="App.openAuthorBooks(${a.id}, '${this.escapeJsString(a.name)}', '${this.escapeJsString(a.bio || '')}')">
                    <div class="author-avatar-img-wrap">
                        <img src="${photoUrl}" alt="${this.escapeHtml(a.name)}" class="author-avatar-photo" referrerpolicy="no-referrer" onerror="this.src='https://ui-avatars.com/api/?name=${encodeURIComponent(a.name)}&background=f97316&color=fff'">
                    </div>
                    <div class="author-details-text">
                        <h4>${this.escapeHtml(a.name)}</h4>
                        <p>${this.escapeHtml(a.bio || 'Renowned author in the BookBasket catalog.')}</p>
                        <span class="author-view-books-btn">View Written Books ➔</span>
                    </div>
                </div>
            `;
        }).join('');
    },

    async openAuthorBooks(authorId, authorName, authorBio) {
        const listView = document.getElementById('authors-list-view');
        const spotView = document.getElementById('author-spotlight-view');
        const headerCard = document.getElementById('author-spotlight-header-card');
        const heading = document.getElementById('author-books-heading');
        const grid = document.getElementById('author-books-grid');

        if (listView) listView.style.display = 'none';
        if (spotView) spotView.style.display = 'block';

        const photoUrl = this.getAuthorPhoto(authorName);

        if (headerCard) {
            headerCard.innerHTML = `
                <div class="author-spotlight-header">
                    <div class="author-spotlight-photo-wrap">
                        <img src="${photoUrl}" alt="${this.escapeHtml(authorName)}" class="author-avatar-photo" referrerpolicy="no-referrer" onerror="this.src='https://ui-avatars.com/api/?name=${encodeURIComponent(authorName)}&background=f97316&color=fff'">
                    </div>
                    <div class="author-spotlight-info">
                        <span class="hero-badge" style="font-size: 0.75rem;">✍️ Featured Author</span>
                        <h3 style="margin-top: 4px;">${this.escapeHtml(authorName)}</h3>
                        <p>${this.escapeHtml(authorBio || 'Distinguished author in the BookBasket library.')}</p>
                    </div>
                </div>
            `;
        }

        if (heading) heading.textContent = `Books Written by ${authorName}`;

        if (grid) {
            grid.innerHTML = `
                <div style="grid-column: 1/-1; text-align: center; padding: 30px;">
                    <div class="spinner-book"></div>
                    <p style="color: var(--text-muted); margin-top: 8px;">Loading books by ${this.escapeHtml(authorName)}...</p>
                </div>
            `;
        }

        // Smooth scroll up to author spotlight
        const authorSec = document.getElementById('section-authors');
        if (authorSec) {
            authorSec.scrollIntoView({ behavior: 'smooth', block: 'start' });
        }

        try {
            const res = await fetch(`${API_BASE}/authors/${authorId}/books`);
            const json = await res.json();
            if (json.success && grid) {
                if (!json.data || json.data.length === 0) {
                    grid.innerHTML = `
                        <div style="grid-column: 1/-1; text-align: center; padding: 40px; background: #ffffff; border-radius: var(--radius-md); border: 1px solid var(--border-light);">
                            <p style="color: var(--text-muted);">No books found for this author yet.</p>
                        </div>
                    `;
                    return;
                }

                grid.innerHTML = json.data.map(book => {
                    const authors = book.authors ? book.authors.map(a => a.name).join(', ') : authorName;
                    const catName = book.category ? book.category.name : 'General';
                    const catId = book.category ? book.category.id : 1;
                    const available = book.availableCopies;
                    const total = book.totalCopies;
                    const coverUrl = book.coverImageUrl || `https://covers.openlibrary.org/b/isbn/${book.isbn}-L.jpg`;

                    return `
                        <div class="user-book-card">
                            <div class="book-cover-wrapper" onclick="App.openBookDetails(${book.id})">
                                <img src="${coverUrl}" alt="${this.escapeHtml(book.title)}" class="book-cover-img" onerror="App.handleImageFallback(this, '${this.escapeJsString(book.title)}', '${this.escapeJsString(authors)}', ${catId})">
                            </div>
                            <div class="book-card-right-details">
                                <div>
                                    <span class="book-genre-pill">${this.escapeHtml(catName)}</span>
                                    <h4 class="book-title-h4" title="${this.escapeHtml(book.title)}" onclick="App.openBookDetails(${book.id})">
                                        ${this.escapeHtml(book.title)}
                                    </h4>
                                    <div class="book-author-line">${this.escapeHtml(authors)}</div>
                                </div>
                                <div>
                                    <div class="book-stock-indicator ${available > 0 ? 'in-stock' : 'out-of-stock'}">
                                        ${available > 0 ? `✅ ${available} of ${total} available` : '❌ Checked out'}
                                    </div>
                                    <div style="display: flex; gap: 6px; align-items: center; margin-top: 6px; flex-wrap: wrap;">
                                        <button class="btn-card-rent" onclick="App.borrowBook(${book.id}, '${this.escapeJsString(book.title)}', ${available})" ${available === 0 ? 'disabled' : ''}>
                                            ${available === 0 ? 'Checked Out' : 'Rent / Borrow'}
                                        </button>
                                        <button class="btn-action-outline red" style="padding: 4px 10px; font-size: 0.75rem; margin-top: 0;" onclick="App.openBookDetails(${book.id})">
                                            View Details
                                        </button>
                                        <button class="btn-sm-read" style="margin-top: 0; padding: 4px 8px; font-size: 0.75rem;" onclick="App.openReadModal(${book.id}, '${this.escapeJsString(book.title)}')">
                                            Read
                                        </button>
                                    </div>
                                </div>
                            </div>
                        </div>
                    `;
                }).join('');
            }
        } catch (e) {
            console.error("Error loading author books:", e);
        }
    },

    // ================= REST API: POST /api/books/{id}/borrow =================
    async borrowBook(bookId, title, availableCopies) {
        if (!this.state.currentUser) {
            this.showToast('info', 'Please sign in to borrow books!');
            this.openLoginModal();
            return;
        }

        if (availableCopies <= 0) {
            this.showToast('error', `Sorry, all copies of "${title}" are currently checked out.`);
            return;
        }

        try {
            const res = await fetch(`${API_BASE}/books/${bookId}/borrow`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ borrowerId: this.state.currentUser.id, loanDays: 14 })
            });

            const json = await res.json();
            if (json.success) {
                this.showToast('success', `"${title}" rented successfully! Added to your shelf. 📖`);
                await Promise.all([
                    this.filterAndRenderBooks(),
                    this.loadUserShelf()
                ]);
            } else {
                this.showToast('error', json.message || 'Could not borrow book.');
            }
        } catch (e) {
            this.showToast('error', 'Server error while borrowing.');
        }
    },

    // ================= REST API: POST /api/books/{id}/return =================
    async returnBook(bookId, transactionId) {
        try {
            const payload = this.state.currentUser ? { borrowerId: this.state.currentUser.id } : {};
            const res = await fetch(`${API_BASE}/books/${bookId}/return`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });

            const json = await res.json();
            if (json.success) {
                this.showToast('success', 'Book returned! Thank you for reading. ✨');
                await Promise.all([
                    this.filterAndRenderBooks(),
                    this.loadUserShelf()
                ]);
                if (document.getElementById('view-my-books')?.classList.contains('active')) {
                    this.loadMyShelfView();
                }
            } else {
                this.showToast('error', json.message || 'Failed to return book.');
            }
        } catch (e) {
            this.showToast('error', 'Server error while returning.');
        }
    },

    // ================= REST API: DELETE /api/books/{id} =================
    async deleteBook(bookId, title) {
        if (!confirm(`Are you sure you want to remove "${title}"?\n\nThis action cannot be undone.`)) {
            return;
        }

        try {
            const res = await fetch(`${API_BASE}/books/${bookId}`, {
                method: 'DELETE'
            });
            const json = await res.json();
            if (json.success) {
                this.showToast('success', `"${title}" has been successfully removed from the library catalog.`);
                this.closeModals();
                await this.filterAndRenderBooks();
                await this.loadStats();
            } else {
                this.showToast('error', json.message || 'Failed to delete book.');
            }
        } catch (e) {
            this.showToast('error', 'Server error while deleting book.');
        }
    },

    // ================= READ ONLINE & EDIT NOTES/BOOKMARKS =================
    currentReadingBookId: null,
    currentReadingBookTitle: '',

    async openReadModal(bookId, bookTitle) {
        if (!this.state.currentUser) {
            this.showToast('info', 'Please sign in to read and annotate books!');
            this.openLoginModal();
            return;
        }

        this.currentReadingBookId = bookId;
        this.currentReadingBookTitle = bookTitle;

        const modal = document.getElementById('read-book-modal');
        const titleEl = document.getElementById('reader-book-title');
        const authorEl = document.getElementById('reader-book-author');
        const descEl = document.getElementById('reader-book-description');

        if (titleEl) titleEl.textContent = bookTitle;

        try {
            const res = await fetch(`${API_BASE}/books/${bookId}`);
            const json = await res.json();
            if (json.success && json.data) {
                const b = json.data;
                if (authorEl) authorEl.textContent = `By ${b.authors?.map(a => a.name).join(', ') || 'Unknown'}`;
                if (descEl) descEl.textContent = b.description || 'Welcome to this digital copy. Enjoy your reading on BookBasket.';
            }
        } catch (e) {}

        // Load reader's saved bookmark
        const userId = this.state.currentUser.id;
        const savedBookmark = localStorage.getItem(`bookmark_user_${userId}_book_${bookId}`) || '';
        const bookmarkBadge = document.getElementById('reader-bookmark-text');
        const bookmarkInp = document.getElementById('reader-bookmark-input');
        if (bookmarkBadge) bookmarkBadge.textContent = savedBookmark ? `Bookmarked: ${savedBookmark}` : 'No bookmark set';
        if (bookmarkInp) bookmarkInp.value = savedBookmark;

        // Load reader's saved notes
        const savedNotes = localStorage.getItem(`notes_user_${userId}_book_${bookId}`) || '';
        const notesArea = document.getElementById('reader-personal-notes-area');
        if (notesArea) notesArea.value = savedNotes;

        // Render saved highlights
        this.renderReaderHighlights();
        this.switchReaderTab('text');

        modal?.classList.add('open');
    },

    switchReaderTab(tab) {
        const textTab = document.getElementById('reader-tab-content-text');
        const notesTab = document.getElementById('reader-tab-content-notes');
        const btnText = document.getElementById('btn-reader-tab-text');
        const btnNotes = document.getElementById('btn-reader-tab-notes');

        if (tab === 'notes') {
            if (textTab) textTab.style.display = 'none';
            if (notesTab) notesTab.style.display = 'block';
            btnText?.classList.remove('active');
            btnNotes?.classList.add('active');
            this.renderReaderHighlights();
        } else {
            if (textTab) textTab.style.display = 'block';
            if (notesTab) notesTab.style.display = 'none';
            btnText?.classList.add('active');
            btnNotes?.classList.remove('active');
        }
    },

    saveReaderBookmark() {
        if (!this.state.currentUser || !this.currentReadingBookId) return;
        const inp = document.getElementById('reader-bookmark-input');
        const val = inp?.value.trim();
        const userId = this.state.currentUser.id;

        if (val) {
            localStorage.setItem(`bookmark_user_${userId}_book_${this.currentReadingBookId}`, val);
            const badge = document.getElementById('reader-bookmark-text');
            if (badge) badge.textContent = `Bookmarked: ${val}`;
            this.showToast('success', `Bookmark saved at: "${val}" 🔖`);
        } else {
            localStorage.removeItem(`bookmark_user_${userId}_book_${this.currentReadingBookId}`);
            const badge = document.getElementById('reader-bookmark-text');
            if (badge) badge.textContent = 'No bookmark set';
            this.showToast('info', 'Bookmark cleared.');
        }
        this.loadUserShelf();
    },

    addReaderHighlight() {
        if (!this.state.currentUser || !this.currentReadingBookId) return;
        const inp = document.getElementById('reader-new-highlight-inp');
        const text = inp?.value.trim();
        if (!text) {
            this.showToast('error', 'Please enter a quote or key point to highlight.');
            return;
        }

        const userId = this.state.currentUser.id;
        const key = `highlights_user_${userId}_book_${this.currentReadingBookId}`;
        const existing = JSON.parse(localStorage.getItem(key) || '[]');
        existing.unshift({
            text: text,
            date: new Date().toLocaleDateString()
        });
        localStorage.setItem(key, JSON.stringify(existing));

        if (inp) inp.value = '';
        this.showToast('success', 'Point highlighted & saved! 🖍️');
        this.renderReaderHighlights();
        this.loadUserShelf();
    },

    deleteReaderHighlight(index) {
        if (!this.state.currentUser || !this.currentReadingBookId) return;
        const userId = this.state.currentUser.id;
        const key = `highlights_user_${userId}_book_${this.currentReadingBookId}`;
        const existing = JSON.parse(localStorage.getItem(key) || '[]');
        existing.splice(index, 1);
        localStorage.setItem(key, JSON.stringify(existing));
        this.showToast('info', 'Highlight removed.');
        this.renderReaderHighlights();
        this.loadUserShelf();
    },

    saveReaderPersonalNotes() {
        if (!this.state.currentUser || !this.currentReadingBookId) return;
        const notesArea = document.getElementById('reader-personal-notes-area');
        const val = notesArea?.value.trim();
        const userId = this.state.currentUser.id;

        localStorage.setItem(`notes_user_${userId}_book_${this.currentReadingBookId}`, val || '');
        this.showToast('success', 'Personal notes saved! 💾');
        this.loadUserShelf();
    },

    renderReaderHighlights() {
        if (!this.state.currentUser || !this.currentReadingBookId) return;
        const userId = this.state.currentUser.id;
        const key = `highlights_user_${userId}_book_${this.currentReadingBookId}`;
        const list = JSON.parse(localStorage.getItem(key) || '[]');

        const countEl = document.getElementById('reader-notes-count');
        if (countEl) countEl.textContent = list.length;

        const container = document.getElementById('reader-highlights-list-container');
        if (!container) return;

        if (list.length === 0) {
            container.innerHTML = `<p style="font-size: 0.8125rem; color: var(--text-muted); padding: 10px; text-align: center;">No highlighted points yet. Add your favorite quotes and lessons above!</p>`;
        } else {
            container.innerHTML = list.map((item, idx) => `
                <div style="background: #fffdf5; border-left: 4px solid #f59e0b; border: 1px solid #fde68a; border-left-width: 4px; border-radius: 6px; padding: 10px 14px; margin-bottom: 8px; display: flex; justify-content: space-between; align-items: center; gap: 10px;">
                    <div style="flex: 1;">
                        <p style="margin: 0; font-size: 0.875rem; color: #78350f; font-weight: 500;">“${this.escapeHtml(item.text)}”</p>
                        <span style="font-size: 0.6875rem; color: var(--text-muted); margin-top: 3px; display: inline-block;">Saved on ${item.date}</span>
                    </div>
                    <button style="background: transparent; border: none; color: #ef4444; font-size: 0.875rem; cursor: pointer;" title="Delete highlight" onclick="App.deleteReaderHighlight(${idx})">✕</button>
                </div>
            `).join('');
        }
    },

    // ================= BOOK DETAILS MODAL WITH REVIEWS =================
    async openBookDetails(bookId) {
        const modal = document.getElementById('book-details-modal');
        const body = document.getElementById('book-details-content-body');
        if (!modal || !body) return;

        body.innerHTML = `<div style="text-align: center; padding: 40px;"><p>Opening book details...</p></div>`;
        modal.classList.add('open');

        try {
            const [bookRes, reviewsRes, historyRes, recsRes] = await Promise.all([
                fetch(`${API_BASE}/books/${bookId}`).then(r => r.json()),
                fetch(`${API_BASE}/books/${bookId}/reviews`).then(r => r.json()).catch(() => ({ success: false, data: [] })),
                fetch(`${API_BASE}/books/${bookId}/history`).then(r => r.json()).catch(() => ({ success: false, data: [] })),
                fetch(`${API_BASE}/recommendations/${bookId}`).then(r => r.json()).catch(() => ({ success: false, data: [] }))
            ]);

            if (bookRes.success && bookRes.data) {
                const b = bookRes.data;
                const reviews = reviewsRes.success ? reviewsRes.data : [];
                const history = historyRes.success ? historyRes.data : [];
                const recs = recsRes.success ? recsRes.data : [];
                const authors = b.authors ? b.authors.map(a => a.name).join(', ') : 'Unknown';
                const cover = b.coverImageUrl || `https://covers.openlibrary.org/b/isbn/${b.isbn}-L.jpg`;

                // Calculate average rating
                const avgRating = reviews.length > 0 
                    ? (reviews.reduce((acc, r) => acc + r.rating, 0) / reviews.length).toFixed(1)
                    : null;

                // Check if current user has an active loan on this book
                const userActiveLoan = this.state.currentUser 
                    ? history.find(h => h.borrowerId === this.state.currentUser.id && (h.status === 'BORROWED' || h.status === 'OVERDUE'))
                    : null;

                const defaultReviewerName = this.state.currentUser ? this.state.currentUser.name : '';

                body.innerHTML = `
                    <div style="display: flex; gap: 24px; flex-wrap: wrap;">
                        <div class="book-cover-wrapper" style="width: 130px; height: 190px;">
                            <img src="${cover}" alt="Cover" class="book-cover-img" onerror="App.handleImageFallback(this, '${this.escapeJsString(b.title)}', '${this.escapeJsString(authors)}', ${b.category?.id || 1})">
                        </div>
                        <div style="flex: 1; min-width: 240px; display: flex; flex-direction: column; gap: 8px;">
                            <div style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 6px;">
                                <span class="book-genre-pill">${this.escapeHtml(b.category ? b.category.name : 'General')}</span>
                                ${avgRating ? `<span class="star-rating-display">⭐ ${avgRating} / 5 (${reviews.length} reviews)</span>` : `<span style="font-size: 0.75rem; color: var(--text-muted);">No reviews yet</span>`}
                            </div>

                            <h2 style="font-size: 1.4rem; color: var(--text-main);">${this.escapeHtml(b.title)}</h2>
                            <p style="font-size: 0.875rem; color: var(--text-muted);">By <strong>${this.escapeHtml(authors)}</strong> (${b.publicationYear || 'N/A'})</p>
                            <p style="font-size: 0.75rem; color: var(--text-subtle);">ISBN: <strong>${this.escapeHtml(b.isbn)}</strong> | Total Copies: <strong>${b.totalCopies}</strong> | Available: <strong>${b.availableCopies}</strong></p>
                            
                            <div class="book-stock-indicator ${b.availableCopies > 0 ? 'in-stock' : 'out-of-stock'}" style="width: fit-content; margin: 4px 0;">
                                ${b.availableCopies > 0 ? `🟢 Available (${b.availableCopies} of ${b.totalCopies} copies)` : '🔴 Currently Checked Out (0 copies available)'}
                            </div>

                            <p style="font-size: 0.875rem; color: #334155; line-height: 1.6; margin-top: 4px;">${this.escapeHtml(b.description || 'No synopsis provided.')}</p>

                            <div style="display: flex; gap: 8px; margin-top: 14px; flex-wrap: wrap; align-items: center;">
                                <button class="btn-action-solid red" style="padding: 7px 18px; font-size: 0.8125rem;" onclick="App.borrowBook(${b.id}, '${this.escapeJsString(b.title)}', ${b.availableCopies})" ${b.availableCopies === 0 ? 'disabled' : ''}>
                                    ${b.availableCopies > 0 ? `Borrow Book` : 'Unavailable'}
                                </button>
                                ${userActiveLoan ? `
                                    <button class="btn-action-solid" style="background: #10b981; color: white; padding: 7px 16px; font-size: 0.8125rem;" onclick="App.returnBook(${b.id}, '${this.escapeJsString(b.title)}', ${userActiveLoan.id}); App.openBookDetails(${b.id});">
                                        Return Book
                                    </button>
                                ` : ''}
                                <button class="btn-action-outline red" style="padding: 6px 14px; font-size: 0.8125rem;" onclick="App.openReadModal(${b.id}, '${this.escapeJsString(b.title)}')">
                                    📖 Read Preview
                                </button>
                            </div>
                        </div>
                    </div>

                    <!-- ================= REVIEWS & RATINGS SECTION ================= -->
                    <div class="reviews-section-card">
                        <div class="reviews-header-flex">
                            <h4 style="font-size: 1rem; color: var(--text-main); font-weight: 700; margin: 0;">
                                ⭐ Reader Reviews & Ratings (${reviews.length})
                            </h4>
                            ${avgRating ? `<span style="font-size: 0.875rem; font-weight: 700; color: #f59e0b;">Average: ${avgRating} / 5</span>` : ''}
                        </div>

                        <!-- Write Review Form -->
                        <form class="review-form-box" onsubmit="App.submitReview(event, ${b.id})">
                            <h5 style="font-size: 0.875rem; color: #1e293b; margin-bottom: 10px; font-weight: 700;">✍️ Leave a Review for this Book:</h5>
                            <div style="display: flex; gap: 12px; margin-bottom: 10px; flex-wrap: wrap; align-items: center;">
                                <div style="flex: 1; min-width: 180px;">
                                    <label style="font-size: 0.75rem; font-weight: 700; color: #475569; display: block; margin-bottom: 4px;">Your Name / Reader Handle:</label>
                                    <input type="text" id="review-name-${b.id}" class="clean-text-input" style="padding: 6px 10px; font-size: 0.8125rem;" value="${this.escapeHtml(defaultReviewerName)}" placeholder="e.g. Alex M." required>
                                </div>
                                <div>
                                    <label style="font-size: 0.75rem; font-weight: 700; color: #475569; display: block; margin-bottom: 4px;">Your Rating:</label>
                                    <div class="star-rating-picker">
                                        <input type="radio" id="star5-${b.id}" name="review-rating-${b.id}" value="5" checked><label for="star5-${b.id}">★</label>
                                        <input type="radio" id="star4-${b.id}" name="review-rating-${b.id}" value="4"><label for="star4-${b.id}">★</label>
                                        <input type="radio" id="star3-${b.id}" name="review-rating-${b.id}" value="3"><label for="star3-${b.id}">★</label>
                                        <input type="radio" id="star2-${b.id}" name="review-rating-${b.id}" value="2"><label for="star2-${b.id}">★</label>
                                        <input type="radio" id="star1-${b.id}" name="review-rating-${b.id}" value="1"><label for="star1-${b.id}">★</label>
                                    </div>
                                </div>
                            </div>
                            <div style="margin-bottom: 10px;">
                                <label style="font-size: 0.75rem; font-weight: 700; color: #475569; display: block; margin-bottom: 4px;">Your Review / Thoughts:</label>
                                <textarea id="review-comment-${b.id}" class="clean-text-input" style="padding: 8px 10px; font-size: 0.8125rem;" rows="2" placeholder="What did you think of the story, writing style, or lessons?" required></textarea>
                            </div>
                            <button type="submit" class="btn-action-solid red" style="padding: 6px 16px; font-size: 0.8125rem;">
                                Submit Review ✨
                            </button>
                        </form>

                        <!-- Existing Reviews List -->
                        <div style="margin-top: 14px;">
                            ${reviews.length === 0 ? `
                                <p style="font-size: 0.8125rem; color: var(--text-muted); text-align: center; padding: 12px;">Be the first reader to review "${this.escapeHtml(b.title)}"! Write your review above.</p>
                            ` : reviews.map(r => `
                                <div class="review-item-card">
                                    <div class="review-item-top">
                                        <span class="review-author-name">👤 ${this.escapeHtml(r.reviewerName)}</span>
                                        <span class="star-rating-display">${'★'.repeat(r.rating)}${'☆'.repeat(5 - r.rating)}</span>
                                    </div>
                                    <p class="review-body-comment">${this.escapeHtml(r.comment)}</p>
                                    <span class="review-date-badge">${r.createdAt ? r.createdAt.substring(0, 10) : 'Recent'}</span>
                                </div>
                            `).join('')}
                        </div>
                    </div>

                    <!-- Borrowing History Section -->
                    <div style="margin-top: 24px; padding-top: 16px; border-top: 1px solid var(--border-light);">
                        <h4 style="font-size: 0.9375rem; color: var(--text-main); margin-bottom: 8px;">📜 Borrowing History (${history.length} records)</h4>
                        ${history.length === 0 ? `
                            <p style="font-size: 0.8125rem; color: var(--text-muted);">No borrowing transactions recorded yet for this book.</p>
                        ` : `
                            <div style="max-height: 150px; overflow-y: auto; border: 1px solid var(--border-light); border-radius: 6px;">
                                <table style="width: 100%; border-collapse: collapse; font-size: 0.75rem; text-align: left;">
                                    <thead style="background: #f8fafc; border-bottom: 1px solid var(--border-light);">
                                        <tr>
                                            <th style="padding: 8px;">Borrower</th>
                                            <th style="padding: 8px;">Borrowed On</th>
                                            <th style="padding: 8px;">Due Date</th>
                                            <th style="padding: 8px;">Status</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        ${history.map(h => `
                                            <tr style="border-bottom: 1px solid #f1f5f9;">
                                                <td style="padding: 8px; font-weight: 600;">${this.escapeHtml(h.borrowerName)}</td>
                                                <td style="padding: 8px; color: var(--text-muted);">${h.borrowedAt ? h.borrowedAt.substring(0, 10) : ''}</td>
                                                <td style="padding: 8px; color: var(--text-muted);">${h.dueDate ? h.dueDate.substring(0, 10) : ''}</td>
                                                <td style="padding: 8px;">
                                                    <span style="padding: 2px 8px; border-radius: 12px; font-size: 0.6875rem; font-weight: 700; background: ${h.status === 'RETURNED' ? '#d1fae5; color: #065f46;' : '#fee2e2; color: #991b1b;'}">
                                                        ${h.status}
                                                    </span>
                                                </td>
                                            </tr>
                                        `).join('')}
                                    </tbody>
                                </table>
                            </div>
                        `}
                    </div>

                    ${recs.length > 0 ? `
                        <div style="margin-top: 20px; padding-top: 14px; border-top: 1px solid var(--border-light);">
                            <h4 style="font-size: 0.9375rem; color: var(--text-main); margin-bottom: 10px;">✨ Readers Also Enjoyed:</h4>
                            <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(180px, 1fr)); gap: 10px;">
                                ${recs.map(r => `
                                    <div style="background: #f8fafc; border: 1px solid var(--border-light); border-radius: 8px; padding: 10px; cursor: pointer;" onclick="App.openBookDetails(${r.bookId})">
                                        <strong style="font-size: 0.8125rem; color: var(--text-main);">${this.escapeHtml(r.title)}</strong>
                                        <div style="font-size: 0.6875rem; color: var(--primary-red); margin-top: 2px;">💡 ${this.escapeHtml(r.reason)}</div>
                                    </div>
                                `).join('')}
                            </div>
                        </div>
                    ` : ''}
                `;
            }
        } catch (e) {
            console.error("Error opening details:", e);
        }
    },

    // ================= SUBMIT COMMUNITY REVIEW =================
    async submitReview(e, bookId) {
        e.preventDefault();
        const nameInp = document.getElementById(`review-name-${bookId}`)?.value.trim();
        const ratingInp = document.querySelector(`input[name="review-rating-${bookId}"]:checked`)?.value || 5;
        const commentInp = document.getElementById(`review-comment-${bookId}`)?.value.trim();

        if (!commentInp) {
            this.showToast('error', 'Please write a brief review comment.');
            return;
        }

        try {
            const res = await fetch(`${API_BASE}/books/${bookId}/reviews`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    reviewerName: nameInp || (this.state.currentUser ? this.state.currentUser.name : 'Anonymous Reader'),
                    rating: parseInt(ratingInp),
                    comment: commentInp
                })
            });
            const json = await res.json();
            if (json.success) {
                this.showToast('success', 'Review posted successfully! Thank you! ✨');
                await this.openBookDetails(bookId);
            } else {
                this.showToast('error', json.message || 'Failed to post review.');
            }
        } catch (err) {
            this.showToast('error', 'Server error while submitting review.');
        }
    },

    // ================= SHELF EDIT & REMOVE ACTIONS =================
    activeShelfLoan: null,

    openShelfNotesModal(loanId, bookTitle, bookId) {
        this.activeShelfLoan = { loanId, bookTitle, bookId };
        const modal = document.getElementById('shelf-notes-modal');
        const titleEl = document.getElementById('shelf-modal-book-title');
        const notesEl = document.getElementById('shelf-personal-notes');
        
        if (titleEl) titleEl.textContent = bookTitle;
        if (notesEl) {
            const saved = localStorage.getItem(`shelf_notes_${loanId}`) || '';
            notesEl.value = saved;
        }
        modal?.classList.add('open');
    },

    saveShelfNotes() {
        if (!this.activeShelfLoan) return;
        const notesEl = document.getElementById('shelf-personal-notes');
        if (notesEl) {
            localStorage.setItem(`shelf_notes_${this.activeShelfLoan.loanId}`, notesEl.value);
            this.showToast('success', `Shelf notes saved for "${this.activeShelfLoan.bookTitle}"! 💾`);
        }
        this.closeModals();
    },

    async deleteFromShelfModal() {
        if (!this.activeShelfLoan) return;
        const { bookId, bookTitle } = this.activeShelfLoan;
        this.closeModals();
        await this.deleteFromShelf(bookId, bookTitle);
    },

    async deleteFromShelf(bookId, bookTitle) {
        if (!this.state.currentUser) return;
        if (!confirm(`Are you sure you want to remove "${bookTitle}" from your reading shelf?`)) {
            return;
        }
        await this.returnBook(bookId, bookTitle);
        this.showToast('success', `"${bookTitle}" has been removed from your shelf.`);
    },

    // ================= SKY AI ASSISTANT =================
    handleSkySubmit() {
        const text = document.getElementById('sky-prompt-textarea')?.value.trim();
        if (text) this.runSkyPrompt(text);
    },

    async runSkyPrompt(promptText) {
        if (!promptText) return;
        this.navigateTo('sky');

        const textarea = document.getElementById('sky-prompt-textarea');
        if (textarea) textarea.value = promptText;

        const results = document.getElementById('sky-results-container');
        if (results) {
            results.innerHTML = `
                <div style="text-align: center; padding: 40px;">
                    <p style="color: var(--text-muted);">Sky is analyzing books for "<strong>${this.escapeHtml(promptText)}</strong>"...</p>
                </div>
            `;
        }

        try {
            const res = await fetch(`${API_BASE}/recommendations/sky`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ prompt: promptText })
            });

            const json = await res.json();
            if (json.success && json.data && results) {
                const d = json.data;
                results.innerHTML = `
                    <div style="background: var(--primary-red-light); border-left: 4px solid var(--primary-red); padding: 14px 18px; border-radius: var(--radius-sm); margin-bottom: 20px;">
                        <strong style="color: var(--primary-red);">${this.escapeHtml(d.skyGreeting)}</strong>
                        <p style="color: #475569; font-size: 0.8125rem; margin-top: 2px;">${this.escapeHtml(d.querySummary)}</p>
                    </div>
                    <div class="books-display-grid">
                        ${d.recommendations.map(r => {
                            const cover = r.coverImageUrl || `https://covers.openlibrary.org/b/isbn/${r.bookId}-L.jpg`;
                            return `
                                <div class="user-book-card">
                                    <div class="book-cover-wrapper" onclick="App.openBookDetails(${r.bookId})">
                                        <img src="${cover}" alt="Cover" class="book-cover-img" onerror="App.handleImageFallback(this, '${this.escapeJsString(r.title)}', '${this.escapeJsString(r.authors?.join(', ') || '')}', 1)">
                                    </div>
                                    <div class="book-card-right-details">
                                        <div>
                                            <span class="hero-badge" style="font-size: 0.6875rem; padding: 2px 8px;">🪄 ${this.escapeHtml(r.reason)}</span>
                                            <h4 class="book-title-h4" style="margin-top: 4px;" onclick="App.openBookDetails(${r.bookId})">${this.escapeHtml(r.title)}</h4>
                                            <div class="book-author-line">${this.escapeHtml(r.authors?.join(', ') || '')}</div>
                                        </div>
                                        <div>
                                            <div class="book-stock-indicator ${r.availableCopies > 0 ? 'in-stock' : 'out-of-stock'}">
                                                ${r.availableCopies > 0 ? `✅ ${r.availableCopies} available` : '❌ Checked out'}
                                            </div>
                                            <button class="btn-card-rent" style="margin-top: 6px;" onclick="App.borrowBook(${r.bookId}, '${this.escapeJsString(r.title)}', ${r.availableCopies})" ${r.availableCopies === 0 ? 'disabled' : ''}>
                                                Rent Book
                                            </button>
                                        </div>
                                    </div>
                                </div>
                            `;
                        }).join('')}
                    </div>
                `;
            }
        } catch (e) {
            if (results) results.innerHTML = `<p style="color: var(--primary-red); text-align: center; padding: 20px;">Could not connect to Sky AI. Please try again.</p>`;
        }
    },

    // ================= MY SHELF VIEW =================
    async loadMyShelfView() {
        const tbody = document.getElementById('my-shelf-table-body');
        if (!tbody) return;

        if (!this.state.currentUser) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="5" style="text-align: center; padding: 40px;">
                        <p style="color: var(--text-muted);">Please log in to view your borrowed books and reading history.</p>
                        <button class="btn-action-solid red" style="margin-top: 12px;" onclick="App.openLoginModal()">Sign In Now</button>
                    </td>
                </tr>
            `;
            return;
        }

        try {
            const res = await fetch(`${API_BASE}/borrowers/${this.state.currentUser.id}/history`);
            const json = await res.json();
            if (json.success) {
                if (json.data.length === 0) {
                    tbody.innerHTML = `<tr><td colspan="5" style="text-align: center; padding: 40px; color: var(--text-muted);">You have not borrowed any books yet. Browse the catalog to borrow your first book!</td></tr>`;
                    return;
                }

                tbody.innerHTML = json.data.map(t => {
                    const isBorrow = t.status === 'BORROWED';
                    const isOverdue = t.status === 'OVERDUE' || (isBorrow && t.overdue);

                    return `
                        <tr>
                            <td><strong>${this.escapeHtml(t.bookTitle)}</strong></td>
                            <td>${t.borrowedAt ? t.borrowedAt.substring(0, 10) : 'N/A'}</td>
                            <td>${t.dueDate ? t.dueDate.substring(0, 10) : 'N/A'}</td>
                            <td>
                                <span class="hero-badge" style="background: ${isOverdue ? '#fee2e2' : isBorrow ? '#e0f2fe' : '#dcfce7'}; color: ${isOverdue ? '#b91c1c' : isBorrow ? '#0369a1' : '#15803d'};">
                                    ${isOverdue ? 'OVERDUE' : t.status}
                                </span>
                            </td>
                            <td>
                                ${(isBorrow || isOverdue) ? `
                                    <button class="btn-sm-return" onclick="App.returnBook(${t.bookId}, ${t.id})">Return Book</button>
                                    <button class="btn-sm-read" style="margin-left: 6px;" onclick="App.openReadModal(${t.bookId}, '${this.escapeJsString(t.bookTitle)}')">Read</button>
                                ` : '<span style="color: var(--accent-emerald); font-size: 0.8125rem;">✓ Returned</span>'}
                            </td>
                        </tr>
                    `;
                }).join('');
            }
        } catch (e) {
            console.error("Error loading shelf table:", e);
        }
    },

    // ================= INSIGHTS VIEW =================
    async loadInsights() {
        try {
            const [dashRes, mostRes, catRes] = await Promise.all([
                fetch(`${API_BASE}/dashboard`).then(r => r.json()),
                fetch(`${API_BASE}/analytics/most-borrowed?limit=8`).then(r => r.json()),
                fetch(`${API_BASE}/analytics/category-stats`).then(r => r.json())
            ]);

            if (dashRes.success) {
                const d = dashRes.data;
                document.getElementById('kpi-total-books').textContent = d.totalBooks;
                document.getElementById('kpi-available-copies').textContent = d.availableCopies;
                document.getElementById('kpi-borrowed-copies').textContent = d.borrowedBooks;
                document.getElementById('kpi-total-borrowers').textContent = d.totalBorrowers;
            }

            const mostList = document.getElementById('insights-most-borrowed-list');
            if (mostList && mostRes.success) {
                mostList.innerHTML = mostRes.data.map((b, idx) => `
                    <div style="display: flex; justify-content: space-between; padding: 10px 0; border-bottom: 1px solid var(--border-light); font-size: 0.875rem;">
                        <span><strong>#${idx + 1}</strong> ${this.escapeHtml(b.title)}</span>
                        <span style="color: var(--primary-red); font-weight: 700;">${b.borrowCount} checkouts</span>
                    </div>
                `).join('');
            }

            const catList = document.getElementById('insights-category-list');
            if (catList && catRes.success) {
                catList.innerHTML = catRes.data.map(cat => `
                    <div style="margin-bottom: 10px; font-size: 0.8125rem;">
                        <div style="display: flex; justify-content: space-between; margin-bottom: 3px;">
                            <span>${this.escapeHtml(cat.categoryName)}</span>
                            <strong style="color: var(--primary-red);">${cat.percentage}%</strong>
                        </div>
                        <div style="height: 5px; background: #e2e8f0; border-radius: 4px; overflow: hidden;">
                            <div style="height: 100%; width: ${cat.percentage}%; background: var(--primary-red);"></div>
                        </div>
                    </div>
                `).join('');
            }
        } catch (e) {
            console.error("Error loading insights:", e);
        }
    },

    // ================= REST API: POST & PUT /api/books =================
    async openEditBookModal(bookId) {
        this.closeModals();
        try {
            const res = await fetch(`${API_BASE}/books/${bookId}`);
            const json = await res.json();
            if (json.success && json.data) {
                this.openAddBookModal(json.data);
            }
        } catch (e) {
            console.error("Error opening edit modal:", e);
        }
    },

    openAddBookModal(bookToEdit = null) {
        if (!this.state.currentUser) {
            this.showToast('info', 'Please sign in to publish books!');
            this.openLoginModal();
            return;
        }

        if (!this.isCurrentUserAuthor()) {
            this.showToast('info', 'To publish and add books to BookBasket, please register as an Author first!');
            this.openRegisterAuthorModal();
            return;
        }

        const authorProfile = this.getLoggedAuthorProfile();
        const modal = document.getElementById('add-book-modal');
        const modalTitle = document.getElementById('add-book-modal-title');
        const btnSubmit = document.getElementById('btn-submit-book');
        const hiddenId = document.getElementById('edit-book-id');
        const titleInp = document.getElementById('add-book-title');
        const isbnInp = document.getElementById('add-book-isbn');
        const yearInp = document.getElementById('add-book-year');
        const copiesInp = document.getElementById('add-book-copies');
        const catSelect = document.getElementById('add-book-category');
        const authSelect = document.getElementById('add-book-authors');
        const coverInp = document.getElementById('add-book-cover');
        const descInp = document.getElementById('add-book-desc');

        if (bookToEdit) {
            if (modalTitle) modalTitle.textContent = "✏️ Edit Book Details";
            if (btnSubmit) btnSubmit.textContent = "Update Book Details";
            if (hiddenId) hiddenId.value = bookToEdit.id;
            if (titleInp) titleInp.value = bookToEdit.title;
            if (isbnInp) isbnInp.value = bookToEdit.isbn;
            if (yearInp) yearInp.value = bookToEdit.publicationYear || '';
            if (copiesInp) copiesInp.value = bookToEdit.totalCopies;
            if (catSelect && bookToEdit.category) catSelect.value = bookToEdit.category.id;
            if (coverInp) coverInp.value = bookToEdit.coverImageUrl || '';
            if (descInp) descInp.value = bookToEdit.description || '';

            if (authSelect && bookToEdit.authors) {
                const aIds = bookToEdit.authors.map(a => a.id);
                Array.from(authSelect.options).forEach(opt => {
                    opt.selected = aIds.includes(parseInt(opt.value));
                });
            }
        } else {
            if (modalTitle) modalTitle.textContent = "➕ Add New Book to Library";
            if (btnSubmit) btnSubmit.textContent = "Publish Book to Catalog";
            if (hiddenId) hiddenId.value = "";
            if (titleInp) titleInp.value = "";
            if (isbnInp) isbnInp.value = "";
            if (yearInp) yearInp.value = "2024";
            if (copiesInp) copiesInp.value = "5";
            if (coverInp) coverInp.value = "";
            if (descInp) descInp.value = "";

            // Automatically pre-select the logged-in author!
            if (authSelect && authorProfile) {
                Array.from(authSelect.options).forEach(opt => {
                    opt.selected = parseInt(opt.value) === authorProfile.id;
                });
            }
        }

        modal?.classList.add('open');
    },

    async handleBookFormSubmit(e) {
        e.preventDefault();
        const editId = document.getElementById('edit-book-id')?.value;
        const title = document.getElementById('add-book-title')?.value.trim();
        const isbn = document.getElementById('add-book-isbn')?.value.trim();
        const year = document.getElementById('add-book-year')?.value;
        const total = document.getElementById('add-book-copies')?.value;
        const catId = document.getElementById('add-book-category')?.value;
        const cover = document.getElementById('add-book-cover')?.value.trim();
        const desc = document.getElementById('add-book-desc')?.value.trim();

        const selectAuth = document.getElementById('add-book-authors');
        const selectedAuthorIds = Array.from(selectAuth?.selectedOptions || []).map(o => parseInt(o.value));

        if (selectedAuthorIds.length === 0) {
            this.showToast('error', 'Please select at least one author.');
            return;
        }

        const payload = {
            title,
            isbn,
            publicationYear: year ? parseInt(year) : null,
            totalCopies: parseInt(total),
            availableCopies: parseInt(total),
            categoryId: parseInt(catId),
            authorIds: selectedAuthorIds,
            coverImageUrl: cover || null,
            description: desc || null
        };

        try {
            let res;
            if (editId) {
                res = await fetch(`${API_BASE}/books/${editId}`, {
                    method: 'PUT',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(payload)
                });
            } else {
                res = await fetch(`${API_BASE}/books`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(payload)
                });
            }

            const json = await res.json();
            if (json.success) {
                this.showToast('success', editId ? `"${title}" updated successfully! ✨` : `"${title}" added to catalog! ✨`);
                this.closeModals();
                await this.filterAndRenderBooks();
                await this.loadStats();
                await this.loadCategories();
                await this.loadAuthors();
            } else {
                this.showToast('error', json.message || 'Failed to save book.');
            }
        } catch (err) {
            this.showToast('error', 'Server error while saving book.');
        }
    },

    // ================= MODAL & TOAST HELPERS =================
    closeModals() {
        document.querySelectorAll('.clean-modal-overlay').forEach(m => m.classList.remove('open'));
    },

    showToast(type, message) {
        const container = document.getElementById('toast-container');
        if (!container) return;

        const toast = document.createElement('div');
        toast.className = `toast-msg ${type}`;
        const icon = type === 'success' ? '✅' : type === 'error' ? '❌' : 'ℹ️';
        toast.innerHTML = `<span>${icon}</span><span>${this.escapeHtml(message)}</span>`;

        container.appendChild(toast);
        setTimeout(() => {
            toast.style.opacity = '0';
            toast.style.transform = 'translateY(10px)';
            toast.style.transition = 'all 0.3s ease';
            setTimeout(() => toast.remove(), 300);
        }, 3500);
    },

    escapeHtml(str) {
        if (!str) return '';
        return String(str)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#039;');
    },

    escapeJsString(str) {
        if (!str) return '';
        return String(str).replace(/'/g, "\\'").replace(/"/g, '\\"');
    }
};

// Start application when DOM is ready
document.addEventListener('DOMContentLoaded', () => App.init());
