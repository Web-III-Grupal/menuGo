# MenuGo

MenuGo es una aplicación móvil para Android que simula el sistema de pedidos de un local de comida rápida.  
Permite a los clientes consultar el menú por categorías, agregar productos a un carrito y generar pedidos; y a los administradores gestionar el catálogo de productos (CRUD) usando Firebase como backend.

---

## 1. Funcionalidades principales

### Autenticación y roles

- Registro e inicio de sesión con **Firebase Authentication (email/contraseña)**.
- Al registrarse se guarda un documento en la colección **`users`** de Firestore con:
    - `uid`, `name`, `email`, `role`, `photoUrl` (opcional).
- Dos tipos de usuario:
    - **Cliente (`client`)**
        - Puede ver el menú, buscar productos, agregar al carrito y confirmar pedidos.
    - **Administrador (`admin`)**
        - Además de lo anterior, puede crear, editar y eliminar productos del menú.

> El rol se guarda en Firestore y se pasa entre pantallas mediante `Intent` para habilitar o deshabilitar opciones de administración (por ejemplo, el FAB de agregar producto y el borrado por “long-click”).

---

### Menú y navegación

- Pantalla principal (`MainActivity`) con las categorías del menú en un **GridLayout**:
    - Hamburguesas
    - Bebidas
    - Postres
    - Acompañamientos
    - Combos
    - Promociones
- Cada categoría abre `ProductListActivity`, que:
    - Carga productos desde la colección **`products`** de Firestore.
    - Filtra por categoría.
    - Permite **buscar por nombre** con un `EditText`.
    - Muestra cada producto en un `RecyclerView` con nombre, descripción, precio e imagen.

---

### Gestión de productos (CRUD – rol admin)

Solo visible cuando el usuario autenticado es **admin**.

- Botón flotante **“Agregar producto”** (`AddProductActivity`):
    - ID, nombre, descripción, categoría, precio.
    - Selección de imagen desde **galería o cámara**.
    - Imagen subida a **Firebase Storage**.
    - Se guarda la URL pública en el campo `imageUri` del documento en `products`.
- Detalle de producto (`ProductDetailActivity`):
    - Muestra información completa del producto.
    - Permite ajustar la cantidad y **agregar al carrito**.
    - Botón “Editar producto” (para admin) → `EditProductActivity`.
- Eliminación:
    - **“Long-click”** sobre un producto muestra un `AlertDialog` de confirmación.
    - Si se confirma, se elimina de Firestore y se actualiza la lista.

---

### Carrito y pedidos

- El carrito se maneja con una clase de apoyo (`CartManager`) en memoria.
- Cada ítem del carrito contiene:
    - `product` (objeto `Product`)
    - `quantity`
- `CartActivity`:
    - Muestra la lista de productos del carrito en un `RecyclerView`.
    - Calcula y muestra el **total del pedido**.
    - Permite **confirmar el pedido**.

Al confirmar un pedido se crea un documento en la colección **`orders`** de Firestore con campos como:

```json
{
  "createdAt": <timestamp>,
  "userId": "<uid del cliente>",
  "items": [
    {
      "productId": 4,
      "name": "Hamburguesa Liam",
      "quantity": 3,
      "unitPrice": 25000,
      "lineTotal": 75000
    }
  ],
  "total": 75000,
  "status": "pendiente"
}
