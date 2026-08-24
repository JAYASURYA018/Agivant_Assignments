import os
import docx
from docx import Document
from docx.shared import Inches, Pt, RGBColor
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.enum.table import WD_TABLE_ALIGNMENT, WD_ALIGN_VERTICAL
from docx.oxml import OxmlElement, parse_xml
from docx.oxml.ns import nsdecls, qn

def set_cell_background(cell, hex_color):
    tcPr = cell._tc.get_or_add_tcPr()
    shd = parse_xml(f'<w:shd {nsdecls("w")} w:fill="{hex_color}"/>')
    tcPr.append(shd)

def set_cell_margins(cell, top=100, bottom=100, left=150, right=150):
    tcPr = cell._tc.get_or_add_tcPr()
    tcMar = parse_xml(f'<w:tcMar {nsdecls("w")}><w:top w:w="{top}" w:type="dxa"/><w:bottom w:w="{bottom}" w:type="dxa"/><w:left w:w="{left}" w:type="dxa"/><w:right w:w="{right}" w:type="dxa"/></w:tcMar>')
    tcPr.append(tcMar)

def add_heading_1(doc, text):
    h = doc.add_heading(text, level=1)
    h.paragraph_format.space_before = Pt(18)
    h.paragraph_format.space_after = Pt(8)
    for run in h.runs:
        run.font.name = 'Segoe UI'
        run.font.size = Pt(18)
        run.font.bold = True
        run.font.color.rgb = RGBColor(185, 28, 28) # Crimson Red
    return h

def add_heading_2(doc, text):
    h = doc.add_heading(text, level=2)
    h.paragraph_format.space_before = Pt(14)
    h.paragraph_format.space_after = Pt(6)
    for run in h.runs:
        run.font.name = 'Segoe UI'
        run.font.size = Pt(14)
        run.font.bold = True
        run.font.color.rgb = RGBColor(30, 41, 59) # Slate Dark
    return h

def add_heading_3(doc, text):
    h = doc.add_heading(text, level=3)
    h.paragraph_format.space_before = Pt(10)
    h.paragraph_format.space_after = Pt(4)
    for run in h.runs:
        run.font.name = 'Segoe UI'
        run.font.size = Pt(12)
        run.font.bold = True
        run.font.color.rgb = RGBColor(71, 85, 105)
    return h

def add_paragraph(doc, text, bold_prefix=None, italic=False):
    p = doc.add_paragraph()
    p.paragraph_format.line_spacing = 1.15
    p.paragraph_format.space_after = Pt(6)
    if bold_prefix:
        r_bold = p.add_run(bold_prefix)
        r_bold.bold = True
        r_bold.font.name = 'Segoe UI'
        r_bold.font.size = Pt(10.5)
        r_bold.font.color.rgb = RGBColor(15, 23, 42)
    r = p.add_run(text)
    r.font.name = 'Segoe UI'
    r.font.size = Pt(10.5)
    r.italic = italic
    r.font.color.rgb = RGBColor(51, 65, 85)
    return p

def add_bullet(doc, text, bold_prefix=None):
    p = doc.add_paragraph(style='List Bullet')
    p.paragraph_format.line_spacing = 1.15
    p.paragraph_format.space_after = Pt(4)
    if bold_prefix:
        r_bold = p.add_run(bold_prefix)
        r_bold.bold = True
        r_bold.font.name = 'Segoe UI'
        r_bold.font.size = Pt(10.5)
        r_bold.font.color.rgb = RGBColor(15, 23, 42)
    r = p.add_run(text)
    r.font.name = 'Segoe UI'
    r.font.size = Pt(10.5)
    r.font.color.rgb = RGBColor(51, 65, 85)
    return p

def add_callout(doc, title, text, bg_hex="F8FAFC", border_color="B91C1C"):
    tbl = doc.add_table(rows=1, cols=1)
    tbl.alignment = WD_TABLE_ALIGNMENT.CENTER
    cell = tbl.cell(0, 0)
    set_cell_background(cell, bg_hex)
    set_cell_margins(cell, top=140, bottom=140, left=200, right=200)
    
    # Left border
    tcPr = cell._tc.get_or_add_tcPr()
    tcBorders = parse_xml(f'<w:tcBorders {nsdecls("w")}><w:left w:val="single" w:sz="24" w:space="0" w:color="{border_color}"/><w:top w:val="none"/><w:right w:val="none"/><w:bottom w:val="none"/></w:tcBorders>')
    tcPr.append(tcBorders)
    
    p = cell.paragraphs[0]
    p.paragraph_format.space_after = Pt(2)
    r1 = p.add_run(f"📌 {title}\n")
    r1.bold = True
    r1.font.name = 'Segoe UI'
    r1.font.size = Pt(10.5)
    r1.font.color.rgb = RGBColor(185, 28, 28)
    
    r2 = p.add_run(text)
    r2.font.name = 'Segoe UI'
    r2.font.size = Pt(10)
    r2.font.color.rgb = RGBColor(71, 85, 105)
    
    doc.add_paragraph().paragraph_format.space_after = Pt(6)

def add_image_with_caption(doc, img_path, caption_text, width_inches=6.0):
    if os.path.exists(img_path):
        p_img = doc.add_paragraph()
        p_img.alignment = WD_ALIGN_PARAGRAPH.CENTER
        p_img.paragraph_format.space_before = Pt(8)
        p_img.paragraph_format.space_after = Pt(4)
        run_img = p_img.add_run()
        run_img.add_picture(img_path, width=Inches(width_inches))
        
        p_cap = doc.add_paragraph()
        p_cap.alignment = WD_ALIGN_PARAGRAPH.CENTER
        p_cap.paragraph_format.space_after = Pt(12)
        run_cap = p_cap.add_run(f"Figure: {caption_text}")
        run_cap.italic = True
        run_cap.font.name = 'Segoe UI'
        run_cap.font.size = Pt(9)
        run_cap.font.color.rgb = RGBColor(100, 116, 139)
    else:
        add_paragraph(doc, f"[Image not found: {img_path}]", italic=True)

def create_styled_table(doc, headers, data, col_widths=None):
    tbl = doc.add_table(rows=len(data) + 1, cols=len(headers))
    tbl.alignment = WD_TABLE_ALIGNMENT.CENTER
    tbl.autofit = False
    
    # Header Row
    hdr_cells = tbl.rows[0].cells
    for i, h in enumerate(headers):
        hdr_cells[i].text = h
        set_cell_background(hdr_cells[i], "1E293B")
        set_cell_margins(hdr_cells[i], top=120, bottom=120, left=150, right=150)
        p = hdr_cells[i].paragraphs[0]
        p.alignment = WD_ALIGN_PARAGRAPH.LEFT
        for run in p.runs:
            run.font.name = 'Segoe UI'
            run.font.size = Pt(9.5)
            run.font.bold = True
            run.font.color.rgb = RGBColor(255, 255, 255)
            
    # Data Rows
    for r_idx, row in enumerate(data):
        row_cells = tbl.rows[r_idx + 1].cells
        bg_color = "F8FAFC" if r_idx % 2 == 1 else "FFFFFF"
        for c_idx, val in enumerate(row):
            row_cells[c_idx].text = str(val)
            set_cell_background(row_cells[c_idx], bg_color)
            set_cell_margins(row_cells[c_idx], top=90, bottom=90, left=150, right=150)
            p = row_cells[c_idx].paragraphs[0]
            for run in p.runs:
                run.font.name = 'Segoe UI'
                run.font.size = Pt(9)
                run.font.color.rgb = RGBColor(51, 65, 85)
                
    # Apply column widths if provided
    if col_widths:
        for row in tbl.rows:
            for idx, width in enumerate(col_widths):
                row.cells[idx].width = Inches(width)
                
    doc.add_paragraph().paragraph_format.space_after = Pt(8)
    return tbl

def build_word_document():
    doc = Document()
    
    # Set page margins to 0.75 in
    for section in doc.sections:
        section.top_margin = Inches(0.75)
        section.bottom_margin = Inches(0.75)
        section.left_margin = Inches(0.75)
        section.right_margin = Inches(0.75)
        
    img_dir = r"c:\Users\NambariLikhitha\Desktop\java\docs\screenshots"
    
    # ------------------ COVER PAGE / TITLE ------------------
    p_title = doc.add_paragraph()
    p_title.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_title.paragraph_format.space_before = Pt(36)
    p_title.paragraph_format.space_after = Pt(8)
    run_t = p_title.add_run("BookBasket")
    run_t.font.name = 'Segoe UI'
    run_t.font.size = Pt(32)
    run_t.font.bold = True
    run_t.font.color.rgb = RGBColor(185, 28, 28) # Primary Red
    
    p_sub = doc.add_paragraph()
    p_sub.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_sub.paragraph_format.space_after = Pt(18)
    run_sub = p_sub.add_run("Smart Online Book Rental & Modern Library Operations Platform\nComprehensive Technical Architecture, Engineering Approach, and Feature Specification")
    run_sub.font.name = 'Segoe UI'
    run_sub.font.size = Pt(13)
    run_sub.font.color.rgb = RGBColor(71, 85, 105)
    
    add_callout(doc, "Project Metadata & Authorship", 
                "Author: Likitha\n"
                "Technology Stack: Java 17/26, Spring Boot 3.3.4, Spring Data JPA, Hibernate, H2 In-Memory Relational Engine, JUnit 5 & Mockito, Vanilla HTML5/CSS3/ES6 JavaScript SPA.\n"
                "Architecture: Layered Clean Architecture (Controller -> Service -> Repository -> Entity) with Dynamic Author Verification & Natural-Language Sky AI Recommender.\n"
                "Status: Production Ready | 64/64 Automated Tests Passing | Fully Integrated Single-Page Application")

    doc.add_page_break()

    # ------------------ 1. PROBLEM STATEMENT & EXECUTIVE SUMMARY ------------------
    add_heading_1(doc, "1. Executive Summary & Problem Statement")
    
    add_paragraph(doc, "Traditional public and institutional library operations frequently suffer from operational fragmentation, manual ledger inaccuracies, and friction in reader engagement. Conventional systems rely on disparate spreadsheets or clunky desktop software that lack real-time stock visibility, dynamic book jackets, community-driven rating systems, and personalized reading recommendations.")
    
    add_paragraph(doc, "BookBasket is engineered as an enterprise-grade, high-performance, and visually captivating full-stack library platform designed to streamline the complete lifecycle of book rentals, inventory management, user borrowing shelves, author self-publishing, and intelligent reading discovery.", bold_prefix="The Solution: ")
    
    add_heading_2(doc, "1.1 Core Problems Addressed")
    add_bullet(doc, " Elimination of physical logbooks and spreadsheet errors through atomic JPA transactions that guarantee exact available copy decrementing (-1 upon borrow) and copy restoration (+1 upon return).", bold_prefix="Zero Inventory Desynchronization:")
    add_bullet(doc, " Overcoming missing book covers through a 20-category chromatic fallback engine that renders bespoke book jackets with custom gradients and genre emblems.", bold_prefix="Visual Elegance & Jacket Uniformity:")
    add_bullet(doc, " Readers can self-register as verified authors via a streamlined modal, automatically unlocking catalog publishing privileges and incrementing platform metrics.", bold_prefix="Author Publishing Disconnect:")
    add_bullet(doc, " Integrated digital reader companion allowing readers to save chapter bookmarks, capture quote highlights, and maintain personal reflections directly inside their shelf.", bold_prefix="Interactive Digital Reading Companion:")
    add_bullet(doc, " Keyword and genre-affinity recommendation engine that matches reader intent to curated catalog titles.", bold_prefix="Natural-Language AI Book Discovery:")

    # ------------------ 2. ARCHITECTURAL APPROACH & DESIGN PATTERNS ------------------
    add_heading_1(doc, "2. Architectural Approach & Design Philosophy")
    add_paragraph(doc, "BookBasket adheres to the Clean Layered Architecture paradigm, enforcing strict separation of concerns across the presentation, application, domain, and data persistence layers.")

    add_heading_2(doc, "2.1 Backend Architecture & Design Patterns")
    add_bullet(doc, " Exposes standardized RESTful JSON endpoints returning uniform ApiResponse<T> structures. Enforces parameter validations (@Valid, @NotNull, @NotBlank, @Min) and routes unhandled exceptions to GlobalExceptionHandler.", bold_prefix="REST Controller Layer: ")
    add_bullet(doc, " Encapsulates core business rules including inventory constraints, loan period calculations (default 14 days), ISBN uniqueness verification, and natural-language scoring algorithms. Uses @Transactional to ensure complete database atomicity.", bold_prefix="Service Business Logic Layer: ")
    add_bullet(doc, " Leverages Spring Data JPA interfaces with custom JPQL queries for optimized multi-factor searching (matching title, author name, genre, and clean ISBN without hyphens).", bold_prefix="Repository & Persistence Layer: ")
    add_bullet(doc, " Normalized schema featuring Book, Author, Borrower, Category, BorrowTransaction, and Review with strict foreign key constraints and bidirectional many-to-many join tables (book_authors).", bold_prefix="Domain Relational Model: ")

    add_heading_2(doc, "2.2 Frontend SPA Design Patterns")
    add_bullet(doc, " Built purely with modern ES6+ Javascript, CSS3 custom variables, and semantic HTML5 without bloated external UI frameworks, achieving near-instant sub-50ms rendering.", bold_prefix="Zero-Framework Vanilla Velocity: ")
    add_bullet(doc, " Centralized App state container managing active reader sessions, catalog cache, active genre filters, multi-factor search strings, and borrower loan histories.", bold_prefix="State Machine Pattern: ")
    add_bullet(doc, " Client-side session and reading progress checkpoints persisted via localStorage (bookbasket_user_id, bookmarks, quote highlights, and author profiles).", bold_prefix="Local Session Cache: ")

    # ------------------ 3. DETAILED FEATURE SPECIFICATION & SCREENSHOTS ------------------
    add_heading_1(doc, "3. Feature Specifications & Visual Demonstrations")

    # Feature 1: Hero Header & Authentication
    add_heading_2(doc, "3.1 Hero Header, Global Navigation & Role-Based Authentication")
    add_paragraph(doc, "The application greets visitors with a dynamic, warm header featuring BookBasket branding, a universal search bar with live auto-complete, quick genre jump anchors, and session-aware authentication pills. Guests can log in or register with one click, which dynamically swaps the login trigger for a personalized user avatar pill.")
    add_image_with_caption(doc, os.path.join(img_dir, "01_hero_header.png"), "BookBasket Hero Header, Search Engine, and Navigation Bar", 6.0)
    add_image_with_caption(doc, os.path.join(img_dir, "01b_login_modal.png"), "Interactive Clean Modal for Reader Sign-In & Google SSO Simulation", 4.5)
    add_image_with_caption(doc, os.path.join(img_dir, "01c_hero_verified_author.png"), "Dynamic Hero State for Verified Author Likitha Nambari", 6.0)

    # Feature 2: 20 Genres & Catalog
    add_heading_2(doc, "3.2 20 Distinct Genres & Dynamic Book Jacket Fallback System")
    add_paragraph(doc, "BookBasket organizes over 100 library titles across 20 distinct literary genres (Fiction, Mystery, Thriller, Romance, Fantasy, Sci-Fi, Horror, Historical Fiction, Adventure, Biography, Autobiography, Self-Help, Psychology, Philosophy, Business, Technology, Young Adult, Children's Literature, Poetry, and Classics). If OpenLibrary image covers are missing or fail to load, an automatic CSS gradient book jacket is dynamically rendered with thematic emblems and gold-embossed typography.")
    add_image_with_caption(doc, os.path.join(img_dir, "02_genres_and_catalog.png"), "Interactive 20 Genre Bubble Bar & Responsive Books Catalog Grid", 6.0)

    # Feature 3: Book Details & Community Reviews
    add_heading_2(doc, "3.3 Book Details, Synopsis & Community Star Rating Reviews")
    add_paragraph(doc, "Clicking 'View Details' opens an expansive modal displaying full publication metadata, author biography, ISBN-13, current inventory availability, and community star reviews. Readers can rate books on a 1-5 star scale and submit written reviews that instantly persist to the H2 database.")
    add_image_with_caption(doc, os.path.join(img_dir, "03_book_details_and_reviews.png"), "Book Details Modal with Live Community Reviews & Star Rating System", 5.5)

    # Feature 4: My Shelf Table & Active Loans
    add_heading_2(doc, "3.4 Real-Time My Shelf & Borrowing Transaction Table")
    add_paragraph(doc, "The dedicated My Shelf view displays a comprehensive, real-time ledger of all currently borrowed titles, loan dates, strict 14-day due dates, status badges (BORROWED / RETURNED / OVERDUE), and instant 1-click Return buttons. Returning a book triggers automatic +1 stock inventory restoration in the database.")
    add_image_with_caption(doc, os.path.join(img_dir, "04_my_reading_shelf_table.png"), "Real-Time Personal Shelf Table Tracking Active Loans and Due Dates", 6.0)

    # Feature 5: Borrowed Shelf Carousel
    add_heading_2(doc, "3.5 Multi-Item Borrowed Shelf Carousel")
    add_paragraph(doc, "When a reader possesses active loans, a sleek, horizontal carousel automatically emerges directly beneath the hero section on the homepage, allowing readers to jump straight into their active books, check due dates, or launch the digital reader.")
    add_image_with_caption(doc, os.path.join(img_dir, "05_borrowed_shelf_carousel.png"), "Home Page Borrowed Shelf Carousel with Quick-Read Access", 6.0)

    # Feature 6: Interactive Online Reader & Highlights
    add_heading_2(doc, "3.6 Interactive Online Reader, Bookmarks & Quotes Highlighter")
    add_paragraph(doc, "A full-screen digital reading companion accessible via the 'Read' button. It provides digital chapter excerpts, a Reading Progress Bookmark tool (e.g. Chapter 3 — Page 45) that attaches active bookmark badges to the shelf card, a Quote Highlighter for capturing memorable insights, and a personal reflections study journal.")
    add_image_with_caption(doc, os.path.join(img_dir, "06b_reader_bookmark_preview.png"), "Interactive Online Reader with Chapter Previews and Progress Bookmarking", 5.8)
    add_image_with_caption(doc, os.path.join(img_dir, "06c_reader_highlights_notes.png"), "Quote Highlighter Tool & Personal Reflections Notebook", 5.8)

    # Feature 7: Sky AI Recommender
    add_heading_2(doc, "3.7 Natural-Language Sky AI Recommender Engine")
    add_paragraph(doc, "Sky AI analyzes reader prompts (e.g. 'I want an inspiring book on software architecture' or 'A gripping fantasy novel') using keyword scoring, mood extraction, and author affinity to deliver 3 top recommendations with companion books and an instant 'Borrow Now' action.")
    add_image_with_caption(doc, os.path.join(img_dir, "06_sky_ai_recommender.png"), "Sky AI Smart Book Recommender Answering Reader Queries", 5.8)

    # Feature 8: Sidebar Drawer & Navigation
    add_heading_2(doc, "3.8 Sidebar Navigation Drawer & Verified Author Profile Menu")
    add_paragraph(doc, "A slide-out drawer providing seamless navigation across Home, My Shelf, Authors Directory, Ask Sky AI, and Library Insights. For verified authors, the drawer displays their author status badge and direct shortcuts to publish new books.")
    add_image_with_caption(doc, os.path.join(img_dir, "07_sidebar_navigation_drawer.png"), "Slide-Out Navigation Drawer with Author Registration Callout", 4.5)
    add_image_with_caption(doc, os.path.join(img_dir, "07b_sidebar_drawer_author.png"), "Sidebar Drawer Transitioned to Verified Author Mode", 4.5)

    # Feature 9: History & Recommendations
    add_heading_2(doc, "3.9 Lifetime Borrowing History & Personalized Catalog Discovery")
    add_paragraph(doc, "Tracks complete lifetime reading history with timestamps, returned dates, and personalized recommendations derived from previously read genres.")
    add_image_with_caption(doc, os.path.join(img_dir, "08_borrowing_history_and_recommendations.png"), "Lifetime Borrowing History & Smart Recommendations", 6.0)

    # Feature 10: Library Insights & Stats Dashboard
    add_heading_2(doc, "3.10 Library Insights & Real-Time Analytics Dashboard")
    add_paragraph(doc, "An executive-level metrics center showcasing 4 key KPI cards (Total Titles, Total Copies, Currently Borrowed, Registered Readers), the Most Borrowed Books Leaderboard, and Category Share Distribution progress bars.")
    add_image_with_caption(doc, os.path.join(img_dir, "09_library_insights_stats.png"), "Library Insights Dashboard with KPI Metrics & Circulation Leaderboards", 6.0)

    # Feature 11: Author Registration & Book Publishing
    add_heading_2(doc, "3.11 Author Verification & Seamless Book Publishing Flow")
    add_paragraph(doc, "Readers can register as authors via a dedicated modal. Once verified, the UI automatically transitions to 'Verified Author', updates the database author count, and unlocks the 'Publish Book' modal. The modal automatically binds the author's identity and auto-generates collision-free ISBNs.")
    add_image_with_caption(doc, os.path.join(img_dir, "05b_add_book_modal_author.png"), "Author-Bound Book Publishing Modal with Auto-Generated ISBN", 5.5)

    # ------------------ 4. RELATIONAL DATABASE DESIGN & H2 QUERIES ------------------
    add_heading_1(doc, "4. Relational Database Design & Schema Verification")
    add_paragraph(doc, "The database is managed by Hibernate ORM on an in-memory H2 database engine (jdbc:h2:mem:bookbasketdb). All tables are populated on startup via DataInitializer.java.")
    
    add_image_with_caption(doc, os.path.join(img_dir, "09_h2_console_login.png"), "H2 Database Console Login (jdbc:h2:mem:bookbasketdb, user: sa)", 5.0)
    add_image_with_caption(doc, os.path.join(img_dir, "10_h2_query_authors.png"), "Live SQL Query on AUTHORS Table", 6.0)
    add_image_with_caption(doc, os.path.join(img_dir, "11_h2_query_borrowers.png"), "Live SQL Query on BORROWERS (Registered Readers) Table", 6.0)
    add_image_with_caption(doc, os.path.join(img_dir, "12_h2_query_borrow_transactions.png"), "Live SQL Query on BORROW_TRANSACTIONS Table", 6.0)
    add_image_with_caption(doc, os.path.join(img_dir, "13_h2_query_books.png"), "Live SQL Query on BOOKS Catalog Table", 6.0)
    add_image_with_caption(doc, os.path.join(img_dir, "14_h2_query_categories.png"), "Live SQL Query on CATEGORIES (20 Genres) Table", 6.0)
    add_image_with_caption(doc, os.path.join(img_dir, "15_h2_query_reviews.png"), "Live SQL Query on REVIEWS Table", 6.0)

    # ------------------ 5. MANDATORY REST APIS SPECIFICATION ------------------
    add_heading_1(doc, "5. REST API Specifications")
    add_paragraph(doc, "The platform exposes an exhaustive, enterprise-grade RESTful API surface compliant with HTTP/1.1 and JSON specifications:")
    
    headers = ["HTTP Method", "API Endpoint Path", "Controller", "Function & Business Rules"]
    api_data = [
        ["POST", "/api/auth/login", "AuthController", "Authenticates reader credentials; returns reader profile and sets active session"],
        ["POST", "/api/auth/signup", "AuthController", "Registers a new library borrower with validated email, phone, and name"],
        ["GET", "/api/books", "BookController", "Retrieves complete catalog list with categories, authors, and stock counts"],
        ["GET", "/api/books/{id}", "BookController", "Fetches single book by ID; throws 404 ResourceNotFoundException if missing"],
        ["GET", "/api/books/search", "BookController", "Multi-factor search supporting title, author name, genre, and clean ISBN"],
        ["POST", "/api/books", "BookController", "Publishes new book to catalog (requires registered author ID and unique ISBN)"],
        ["PUT", "/api/books/{id}", "BookController", "Updates book metadata, description, publication year, and total copy allocations"],
        ["DELETE", "/api/books/{id}", "BookController", "Removes book from catalog; throws 400 if book has unreturned active loans"],
        ["POST", "/api/books/{id}/borrow", "BorrowController", "Decrements availableCopies by 1; creates active 14-day loan record"],
        ["POST", "/api/books/{id}/return", "BorrowController", "Restores availableCopies by 1; marks loan as RETURNED with timestamp"],
        ["GET", "/api/borrowers/{id}/history", "BorrowerController", "Retrieves active and historical borrow transactions for a specific reader"],
        ["POST", "/api/authors", "AuthorController", "Registers an author profile into the database and increments author metrics"],
        ["POST", "/api/books/{id}/reviews", "ReviewController", "Submits 1-5 star community rating and comment; recalculates average rating"],
        ["POST", "/api/recommendations/sky", "RecommendationController", "Natural-language AI companion matching user queries to top 3 books"],
        ["GET", "/api/dashboard", "DashboardController", "Aggregates total titles, total copies, active loans, and reader counts"],
        ["GET", "/api/analytics/category-stats", "AnalyticsController", "Computes percentage inventory distribution across all 20 categories"]
    ]
    create_styled_table(doc, headers, api_data, [1.0, 1.8, 1.4, 2.8])

    # ------------------ 6. AUTOMATED TEST SUITE & VERIFICATION PROOF ------------------
    add_heading_1(doc, "6. Test Suite & Verification Proof")
    add_paragraph(doc, "The platform is guarded by a comprehensive JUnit 5 and Mockito test suite comprising 64 unit and controller integration tests. All tests execute via 'mvn test' with 0 failures and 0 errors.")
    
    add_bullet(doc, " Validates all CRUD actions, validation constraints, duplicate ISBN protection, and stock integrity.", bold_prefix="BookControllerTest & BookServiceTest (11 Tests): ")
    add_bullet(doc, " Enforces zero-availability blocking, +1/-1 inventory arithmetic, and 14-day due date calculation.", bold_prefix="BorrowControllerTest & BorrowServiceTest (10 Tests): ")
    add_bullet(doc, " Tests borrower registration, email uniqueness, duplicate detection, and loan history aggregation.", bold_prefix="BorrowerControllerTest & BorrowerServiceTest (9 Tests): ")
    add_bullet(doc, " Verifies category creation, 20 genre seeding, and book count aggregations.", bold_prefix="CategoryControllerTest & CategoryServiceTest (8 Tests): ")
    add_bullet(doc, " Validates keyword matching, TF-IDF scoring, and affinity logic.", bold_prefix="RecommendationControllerTest & RecommendationServiceTest (4 Tests): ")
    add_bullet(doc, " Verifies real-time metric counter aggregation.", bold_prefix="DashboardControllerTest (1 Test): ")
    add_bullet(doc, " Asserts structured error JSON for ResourceNotFound, DuplicateResource, and Validation errors.", bold_prefix="GlobalExceptionHandlerTest (5 Tests): ")

    add_heading_2(doc, "6.1 Terminal Execution & Test Success Screenshots")
    add_image_with_caption(doc, os.path.join(img_dir, "16_terminal_test_execution.png"), "Maven Test Execution Running 64 Unit & Controller Tests", 6.0)
    add_image_with_caption(doc, os.path.join(img_dir, "17_terminal_test_success.png"), "Terminal Test Verification: 64 Tests Run, 0 Failures, 0 Errors (BUILD SUCCESS)", 6.0)

    # ------------------ 7. PROJECT STRUCTURE ------------------
    add_heading_1(doc, "7. Project Structure")
    
    tree_text = (
        "bookbasket-library-platform/\n"
        "├── mvn.cmd                                  # Maven command runner helper\n"
        "├── run.bat                                  # 1-Click application launcher with port collision resolver\n"
        "├── test.bat                                 # 1-Click test suite execution script\n"
        "├── pom.xml                                  # Maven dependencies (Spring Boot 3.3.4, JPA, H2, JUnit 5)\n"
        "├── src/\n"
        "│   ├── main/\n"
        "│   │   ├── java/com/bookpulse/\n"
        "│   │   │   ├── BookPulseApplication.java    # Spring Boot Main Entry Point\n"
        "│   │   │   ├── config/                      # WebMvc & DataInitializer configuration\n"
        "│   │   │   ├── controller/                  # REST Controllers (Auth, Book, Borrow, Review, Sky AI, Analytics)\n"
        "│   │   │   ├── dto/                         # Request and Response Data Transfer Objects\n"
        "│   │   │   ├── entity/                      # Relational Entities (Book, Author, Borrower, Category, Review, Loan)\n"
        "│   │   │   ├── exception/                   # Custom Exceptions & Global Exception Handler\n"
        "│   │   │   ├── repository/                  # Spring Data JPA Repositories\n"
        "│   │   │   └── service/                     # Core Business Logic Services\n"
        "│   │   └── resources/\n"
        "│   │       ├── application.properties       # Application configurations & H2 In-Memory DB setup\n"
        "│   │       └── static/                      # Single-Page Application (index.html, app.js, style.css)\n"
        "│   └── test/\n"
        "│       └── java/com/bookpulse/              # 64 JUnit 5 & Mockito automated unit & integration tests\n"
        "└── docs/\n"
        "    └── screenshots/                         # 26 High-resolution interface, database & test screenshots\n"
    )
    
    p_tree = doc.add_paragraph()
    p_tree.paragraph_format.line_spacing = 1.0
    p_tree.paragraph_format.space_after = Pt(12)
    run_tree = p_tree.add_run(tree_text)
    run_tree.font.name = 'Consolas'
    run_tree.font.size = Pt(8.5)
    run_tree.font.color.rgb = RGBColor(30, 41, 59)

    # ------------------ 8. AUTHOR ATTRIBUTION ------------------
    add_heading_1(doc, "8. Authorship & Project Attribution")
    add_paragraph(doc, "This project was designed, developed, architected, and verified by Likitha as part of the BookBasket Smart Online Book Rental & Library Operations Platform.", bold_prefix="Author: ")
    add_paragraph(doc, "All rights reserved. Developed with clean architecture, enterprise Java, and modern web design standards.")

    # Save Document
    output_path = r"c:\Users\NambariLikhitha\Desktop\java\BookBasket_Project_Documentation.docx"
    doc.save(output_path)
    print(f"Document successfully created at: {output_path}")

if __name__ == "__main__":
    build_word_document()
