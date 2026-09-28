const API = "/api";

async function loadSupportData() {
  const [authors, categories] = await Promise.all([
    fetch(API + "/authors").then(r => r.json()),
    fetch(API + "/categories").then(r => r.json())
  ]);

  document.getElementById("author").innerHTML =
    authors.map(a => `<option value="${a.id}">${a.name}</option>`).join("");
  document.getElementById("category").innerHTML =
    categories.map(c => `<option value="${c.id}">${c.name}</option>`).join("");
}

async function loadBooks() {
  const keyword = document.getElementById("search").value.trim();
  const url = keyword ? API + "/books/search?keyword=" + encodeURIComponent(keyword) : API + "/books";
  const response = await fetch(url);
  const books = await response.json();

  document.getElementById("books").innerHTML = books.map(book => `
    <div class="book">
      <div>
        <strong>${escapeHtml(book.title)}</strong><br>
        Author: ${escapeHtml(book.author.name)} |
        Category: ${escapeHtml(book.category.name)} |
        Borrowed: ${book.borrowCount} times
        <br><br>
        <span class="badge ${book.availableCopies === 0 ? "unavailable" : ""}">
          ${book.availableCopies} / ${book.totalCopies} available
        </span>
      </div>
      <div class="actions">
        <button onclick="borrowBook(${book.id})" ${book.availableCopies === 0 ? "disabled" : ""}>Borrow</button>
        <button class="secondary" onclick="returnBook(${book.id})">Return</button>
        <button class="secondary" onclick="deleteBook(${book.id})">Delete</button>
      </div>
    </div>
  `).join("") || "<p>No books found.</p>";
}

async function addBook(event) {
  event.preventDefault();

  const total = Number(document.getElementById("totalCopies").value);
  const available = Number(document.getElementById("availableCopies").value);

  if (available > total) {
    showMessage("Available copies cannot be greater than total copies.");
    return;
  }

  const data = {
    title: document.getElementById("title").value.trim(),
    authorId: Number(document.getElementById("author").value),
    categoryId: Number(document.getElementById("category").value),
    totalCopies: total,
    availableCopies: available
  };

  const response = await fetch(API + "/books", {
    method: "POST",
    headers: {"Content-Type": "application/json"},
    body: JSON.stringify(data)
  });

  if (response.ok) {
    document.getElementById("bookForm").reset();
    showMessage("Book added successfully.");
    loadBooks();
  } else {
    const error = await response.json();
    showMessage(error.error || "Could not add book.");
  }
}

async function borrowBook(id) {
  const borrowerId = prompt("Enter borrower ID (1, 2 or 3 for sample data):", "1");
  if (!borrowerId) return;

  const response = await fetch(`${API}/books/${id}/borrow`, {
    method: "POST",
    headers: {"Content-Type": "application/json"},
    body: JSON.stringify({borrowerId: Number(borrowerId)})
  });

  const data = await response.json();
  showMessage(data.message || data.error);
  loadBooks();
}

async function returnBook(id) {
  const response = await fetch(`${API}/books/${id}/return`, {method: "POST"});
  const data = await response.json();
  showMessage(data.message || data.error);
  loadBooks();
}

async function deleteBook(id) {
  if (!confirm("Delete this book?")) return;
  const response = await fetch(`${API}/books/${id}`, {method: "DELETE"});
  const data = await response.json();
  showMessage(data.message || data.error);
  loadBooks();
}

function showMessage(text) {
  document.getElementById("message").textContent = text;
}

function escapeHtml(value) {
  return String(value).replace(/[&<>"']/g, c => ({
    "&":"&amp;", "<":"&lt;", ">":"&gt;", '"':"&quot;", "'":"&#039;"
  }[c]));
}

document.getElementById("bookForm").addEventListener("submit", addBook);
document.getElementById("search").addEventListener("input", loadBooks);

loadSupportData();
loadBooks();
