/* ==============================================
   1. VARIABLES GLOBALES & ESTADO
   ============================================== */
// Usamos la variable global CART_KEY definida en el HTML
// Si no existe (ej: login page), usamos un default seguro
const STORAGE_KEY = (typeof CART_KEY !== 'undefined') ? CART_KEY : 'carrito_guest';

let carrito = JSON.parse(localStorage.getItem(STORAGE_KEY)) || [];

/* ==============================================
   2. INICIALIZACIÓN (EVENT LISTENERS)
   ============================================== */
document.addEventListener("DOMContentLoaded", () => {
    initMenu(); 
    actualizarInterfazCarrito();
    
    // Si estamos en checkout, cargar tabla
    if (window.location.pathname.includes('/checkout')) {
        renderizarCheckout();
    }
});

/* ==============================================
   3. LÓGICA DE DATOS DEL CARRITO (MODELO)
   ============================================== */
function guardarYActualizar() {
    // AQUÍ ESTÁ LA MAGIA: Guardamos con la clave única del usuario
    localStorage.setItem(STORAGE_KEY, JSON.stringify(carrito));
    actualizarInterfazCarrito();
}

function agregarAlCarrito(id, nombre, precio, imagenUrl) {
    id = parseInt(id);
    const existente = carrito.find(prod => prod.id === id);

    if (existente) {
        existente.cantidad++;
    } else {
        carrito.push({
            id,
            nombre,
            precio: parseFloat(precio),
            imagen: imagenUrl || '/img/default-product.png',
            cantidad: 1
        });
    }

    guardarYActualizar();

    const sidebar = document.getElementById('cart-sidebar');
    if (sidebar && !sidebar.classList.contains('open')) toggleCarrito();
}

function cambiarCantidad(id, cambio) {
    const item = carrito.find(prod => prod.id === id);
    if (!item) return;

    item.cantidad += cambio;

    if (item.cantidad <= 0) {
        eliminarDelCarrito(id);
        return;
    }

    guardarYActualizar();

    if (window.location.pathname.includes('/checkout')) {
        renderizarCheckout();
    }
}

function eliminarDelCarrito(id) {
    carrito = carrito.filter(prod => prod.id !== id);
    guardarYActualizar();

    if (window.location.pathname.includes('/checkout')) {
        renderizarCheckout();
    }
}

function vaciarCarrito() {
    carrito = [];
    guardarYActualizar();

    if (window.location.pathname.includes('/checkout')) {
        renderizarCheckout();
    }
}

/* ==============================================
   4. INTERFAZ DE USUARIO DEL CARRITO (VISTA)
   ============================================== */
function actualizarInterfazCarrito() {
    const container = document.getElementById('cart-items-container');
    const contador = document.getElementById('cart-count');
    const precioTotal = document.getElementById('cart-total-price');

    const totalItems = carrito.reduce((acc, prod) => acc + prod.cantidad, 0);
    if (contador) contador.innerText = totalItems;

    const total = carrito.reduce((acc, prod) => acc + (prod.precio * prod.cantidad), 0);
    if (precioTotal) precioTotal.innerText = `S/ ${total.toFixed(2)}`;

    if (!container) return;

    container.innerHTML = '';

    if (carrito.length === 0) {
        container.innerHTML = '<p style="text-align:center;color:#666;margin-top:20px;">Vacío</p>';
        return;
    }

    carrito.forEach(prod => {
        container.innerHTML += `
            <div class="cart-item">
                <img src="${prod.imagen}" style="width:50px;height:50px;object-fit:cover;border-radius:4px;">
                
                <div style="flex:1;">
                    <h4 style="font-size:0.9rem;margin:0;">${prod.nombre}</h4>

                    <div style="display:flex;gap:5px;margin-top:5px;">
                        <button onclick="cambiarCantidad(${prod.id}, -1)" class="btn-sm">-</button>
                        <span>${prod.cantidad}</span>
                        <button onclick="cambiarCantidad(${prod.id}, 1)" class="btn-sm">+</button>
                    </div>
                </div>

                <div style="text-align:right;">
                    <div style="font-weight:bold;">S/ ${(prod.precio * prod.cantidad).toFixed(2)}</div>
                    <span class="cart-item-remove" onclick="eliminarDelCarrito(${prod.id})">
                        <i class="fas fa-trash"></i>
                    </span>
                </div>
            </div>
        `;
    });
}

function toggleCarrito() {
    const sidebar = document.getElementById('cart-sidebar');
    const overlay = document.getElementById('cart-overlay');
    const navMenu = document.getElementById('navMenu');

    if (navMenu) navMenu.classList.remove('active');

    if (sidebar && overlay) {
        sidebar.classList.toggle('open');
        overlay.style.display = sidebar.classList.contains('open') ? 'block' : 'none';
    }
}

function irAlCheckout() {
    if (carrito.length === 0) return alert("Tu carrito está vacío.");
    window.location.href = "/checkout";
}

/* ==============================================
   5. INTERFAZ GENERAL Y NAVEGACIÓN
   ============================================== */
function initMenu() {
    const burgerBtn = document.getElementById('burgerBtn');
    const navMenu = document.getElementById('navMenu'); 

    if (burgerBtn && navMenu) {
        burgerBtn.addEventListener('click', () => {
            navMenu.classList.toggle('active');
            burgerBtn.classList.toggle('toggle');
        });

        navMenu.querySelectorAll('a').forEach(link => {
            link.addEventListener('click', () => navMenu.classList.remove('active'));
        });
    }
}

function filtrarProductos(idCategoria, event) {
    if (event) event.preventDefault();

    document.querySelectorAll('.cat-list a').forEach(link =>
        link.classList.remove('active-cat')
    );

    if (event && event.target) event.target.classList.add('active-cat');

    document.querySelectorAll('.card-producto').forEach(prod => {
        const cat = prod.getAttribute('data-categoria');
        prod.style.display = (idCategoria === 'todos' || cat == idCategoria) ? 'flex' : 'none';
    });
}

/* ==============================================
   6. LÓGICA DE CHECKOUT Y COMPRAS
   ============================================== */
function renderizarCheckout() {
    const container = document.getElementById('checkout-items');
    const totalLabel = document.getElementById('checkout-total');

    if (carrito.length === 0) {
        container.innerHTML = "<p>El carrito está vacío. <a href='/tienda'>Ir a la tienda</a></p>";
        totalLabel.innerText = "S/ 0.00";
        return;
    }

    let html = '<table><thead><tr><th>Producto</th><th>Cant</th><th>Subtotal</th></tr></thead><tbody>';
    let total = 0;

    carrito.forEach(prod => {
        let subtotal = prod.precio * prod.cantidad;
        total += subtotal;

        html += `
            <tr>
                <td>${prod.nombre}<br><small>S/ ${prod.precio}</small></td>
                <td>
                    <button onclick="cambiarCantidad(${prod.id}, -1)" class="btn-sm">-</button>
                    ${prod.cantidad}
                    <button onclick="cambiarCantidad(${prod.id}, 1)" class="btn-sm">+</button>
                </td>
                <td>S/ ${subtotal.toFixed(2)}</td>
            </tr>
        `;
    });

    html += '</tbody></table>';

    container.innerHTML = html;
    totalLabel.innerText = `S/ ${total.toFixed(2)}`;
}

async function procesarCompra() {
    if (carrito.length === 0) return alert("Carrito vacío");

    const items = carrito.map(i => ({
        idproducto: i.id,
        cantidad: i.cantidad,
        precio: i.precio
    }));

    const btn = document.querySelector('.cart-footer .btn-filled');
    if (btn) { btn.innerText = "Procesando..."; btn.disabled = true; }

    try {
        const res = await fetch('/ventas/checkout', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(items)
        });

        const data = await res.json();

        if (res.ok) {
            alert("✅ " + data.message);
            vaciarCarrito();
            toggleCarrito();
        } else if (res.status === 401) {
            alert("⚠️ " + data.message);
            window.location.href = "/login";
        } else {
            alert("❌ " + data.message);
        }

    } catch (e) {
        alert("Error de conexión");
    } finally {
        if (btn) {
            btn.innerText = "Finalizar Compra";
            btn.disabled = false;
        }
    }
}

async function procesarCompraFinal() {
    const direccion = document.getElementById('direccion').value;
    const ciudad = document.getElementById('ciudad').value;
    const referencia = document.getElementById('referencia').value;
    const telefono = document.getElementById('telefono').value;
    const tipoPago = document.getElementById('tipoPago').value;

    if (!direccion || !ciudad || !telefono) {
        alert("Por favor completa los campos obligatorios.");
        return;
    }

    const payload = {
        carrito: carrito.map(item => ({
            idproducto: item.id,
            cantidad: item.cantidad,
            precio: item.precio
        })),
        direccion,
        ciudad,
        referencia,
        telefono,
        tipoPago
    };

    const btn = document.querySelector('button[onclick="procesarCompraFinal()"]');
    if (btn) { btn.disabled = true; btn.innerText = "Procesando..."; }

    try {
        const response = await fetch('/pedidos/guardar', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        const data = await response.json();

        if (data.status === 'success') {
            alert("✅ " + data.message);
            vaciarCarrito();
            window.location.href = "/";
        } else {
            alert("❌ Error: " + data.message);
            if (btn) { btn.disabled = false; btn.innerText = "Confirmar Compra"; }
        }

    } catch (error) {
        console.error(error);
        alert("Error de conexión");
        if (btn) { btn.disabled = false; btn.innerText = "Confirmar Compra"; }
    }
}

/* ==============================================
   7. PANEL DE ADMINISTRACIÓN / PROVEEDORES
   ============================================== */
function showSection(sectionId, btnElement) {
    document.querySelectorAll('.section-panel').forEach(el =>
        el.classList.remove('active')
    );
    document.getElementById('sec-' + sectionId).classList.add('active');

    document.querySelectorAll('.nav-btn').forEach(el =>
        el.classList.remove('active')
    );
    btnElement.classList.add('active');
}

function editarProveedor(id, ruc, razon, tel, email, dir) {
    document.getElementById('prov_id').value = id;
    document.getElementById('prov_ruc').value = ruc;
    document.getElementById('prov_razon').value = razon;
    document.getElementById('prov_tel').value = tel;
    document.getElementById('prov_email').value = email;
    document.getElementById('prov_dir').value = dir;

    document.getElementById('form-title').innerText = "Editar Proveedor Existente";
    document.getElementById('form-title').style.color = "#2563eb";

    document.querySelector('.settings-content').scrollIntoView({ behavior: 'smooth' });
}

function limpiarFormProveedor() {
    document.getElementById('prov_id').value = "";
    document.getElementById('prov_ruc').value = "";
    document.getElementById('prov_razon').value = "";
    document.getElementById('prov_tel').value = "";
    document.getElementById('prov_email').value = "";
    document.getElementById('prov_dir').value = "";

    document.getElementById('form-title').innerText = "Registrar Nuevo Proveedor";
    document.getElementById('form-title').style.color = "#475569";
}