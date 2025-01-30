let orderItems = [];

$(document).ready(function() {
    initializeForm();
    loadCustomers();
    loadItems();
    loadOrderHistory();

    $('#customerSelect').change(onCustomerSelect);
    $('#itemSelect').change(onItemSelect);
    $('#itemQuantity').change(validateQuantity);
    $('#addToOrderBtn').click(addItemToOrder);
    $('#placeOrderBtn').click(placeOrder);
    $('#cancelOrderBtn').click(cancelOrder);
});

function initializeForm() {
    const today = new Date().toISOString().split('T')[0];
    $('#orderDate').val(today);

    $.ajax({
        url: 'http://localhost:8081/Appication_1_Web_exploded/pages/placeOrder',
        method: 'GET',
        data: { option: 'GETID' },
        success: function(response) {
            $('#orderId').val(response.id);
        },
        error: function(xhr, status, error) {
            console.error('Error getting order ID:', error);
            alert('Error getting order ID');
        }
    });
}

function loadCustomers() {
    $.ajax({
        url: 'http://localhost:8081/Appication_1_Web_exploded/pages/placeOrder',
        method: 'GET',
        data: { option: 'CUSTOMERS' },
        success: function(customers) {
            const select = $('#customerSelect');
            select.empty().append('<option value="">Select Customer</option>');
            customers.forEach(customer => {
                select.append(`<option value="${customer.id}" 
                    data-name="${customer.name}" 
                    data-contact="${customer.contact}">
                    ${customer.id} - ${customer.name}
                </option>`);
            });
        },
        error: function(xhr, status, error) {
            console.error('Error loading customers:', error);
            alert('Error loading customers');
        }
    });
}

function loadItems() {
    $.ajax({
        url: 'http://localhost:8081/Appication_1_Web_exploded/pages/placeOrder',
        method: 'GET',
        data: { option: 'ITEMS' },
        success: function(items) {
            const select = $('#itemSelect');
            select.empty().append('<option value="">Select Item</option>');
            items.forEach(item => {
                select.append(`<option value="${item.id}" 
                    data-price="${item.price}" 
                    data-stock="${item.stock}"
                    data-name="${item.name}">
                    ${item.id} - ${item.name}
                </option>`);
            });
        },
        error: function(xhr, status, error) {
            console.error('Error loading items:', error);
            alert('Error loading items');
        }
    });
}

function onCustomerSelect() {
    const selected = $('#customerSelect option:selected');
    $('#customerName').text(selected.data('name') || '-');
    $('#customerContact').text(selected.data('contact') || '-');
}

function onItemSelect() {
    const selected = $('#itemSelect option:selected');
    const price = selected.data('price');
    const stock = selected.data('stock');

    $('#itemPrice').text(price ? `$${price.toFixed(2)}` : '$0.00');
    $('#itemStock').val(stock || '');
    $('#itemQuantity').val(1);
    validateQuantity();
}

function validateQuantity() {
    const quantity = parseInt($('#itemQuantity').val());
    const stock = parseInt($('#itemStock').val());

    if (quantity > stock) {
        alert('Quantity cannot exceed available stock');
        $('#itemQuantity').val(stock);
    } else if (quantity < 1) {
        $('#itemQuantity').val(1);
    }
}

function addItemToOrder() {
    const itemSelect = $('#itemSelect option:selected');
    if (!itemSelect.val()) {
        alert('Please select an item');
        return;
    }

    const itemId = itemSelect.val();
    const quantity = parseInt($('#itemQuantity').val());
    const price = itemSelect.data('price');
    const name = itemSelect.data('name');

    const existingItem = orderItems.find(item => item.itemId === itemId);
    if (existingItem) {
        alert('Item already added to order');
        return;
    }

    orderItems.push({
        itemId,
        name,
        price,
        quantity,
        total: price * quantity
    });

    updateOrderTable();
    $('#itemSelect').val('');
    $('#itemPrice').text('$0.00');
    $('#itemStock').val('');
    $('#itemQuantity').val(1);
}

function updateOrderTable() {
    const tbody = $('#orderTableBody');
    tbody.empty();

    let grandTotal = 0;
    orderItems.forEach((item, index) => {
        grandTotal += item.total;
        tbody.append(`
            <tr>
                <td>${item.itemId}</td>
                <td>${item.name}</td>
                <td>$${item.price.toFixed(2)}</td>
                <td>${item.quantity}</td>
                <td>$${item.total.toFixed(2)}</td>
                <td>
                    <button class="btn btn-danger btn-sm" 
                        onclick="removeOrderItem(${index})">
                        Remove
                    </button>
                </td>
            </tr>
        `);
    });

    $('#grandTotal').text(`$${grandTotal.toFixed(2)}`);
}

function removeOrderItem(index) {
    orderItems.splice(index, 1);
    updateOrderTable();
}

function placeOrder() {
    if (!validateOrder()) return;

    const orderData = {
        orderId: $('#orderId').val(),
        customerId: $('#customerSelect').val(),
        orderDate: $('#orderDate').val(),
        items: orderItems
    };

    $.ajax({
        url: 'http://localhost:8081/Appication_1_Web_exploded/pages/placeOrder',
        method: 'POST',
        contentType: 'application/json',
        data: JSON.stringify(orderData),
        success: function(response) {
            alert('Order placed successfully');
            resetForm();
            loadOrderHistory();
        },
        error: function(xhr, status, error) {
            console.error('Error placing order:', error);
            alert('Error placing order');
        }
    });
}

function validateOrder() {
    if (!$('#customerSelect').val()) {
        alert('Please select a customer');
        return false;
    }

    if (orderItems.length === 0) {
        alert('Please add at least one item to the order');
        return false;
    }

    if (!$('#orderDate').val()) {
        alert('Please select an order date');
        return false;
    }

    return true;
}

function cancelOrder() {
    if (confirm('Are you sure you want to cancel this order? All items will be removed.')) {
        resetForm();
    }
}

function resetForm() {
    orderItems = [];
    updateOrderTable();

    $('#customerSelect').val('');
    $('#customerName').text('-');
    $('#customerContact').text('-');

    $('#itemSelect').val('');
    $('#itemPrice').text('$0.00');
    $('#itemStock').val('');
    $('#itemQuantity').val(1);

    initializeForm();
}

function loadOrderHistory() {
    $.ajax({
        url: 'http://localhost:8081/Appication_1_Web_exploded/pages/placeOrder',
        method: 'GET',
        success: function(orders) {
            const tbody = $('#orderHistoryBody');
            tbody.empty();

            orders.forEach(order => {
                tbody.append(`
                    <tr>
                        <td>${order.id}</td>
                        <td>${formatDate(order.date)}</td>
                        <td>${order.customerId}</td>
                        <td>${order.customerName}</td>
                        <td>${order.itemCount}</td>
                        <td>$${order.totalAmount.toFixed(2)}</td>
                        <td>
                            <button class="btn btn-info btn-sm" 
                                onclick="viewOrderDetails('${order.id}')">
                                View Details
                            </button>
                        </td>
                    </tr>
                `);
            });
        },
        error: function(xhr, status, error) {
            console.error('Error loading order history:', error);
            alert('Error loading order history');
        }
    });
}

function viewOrderDetails(orderId) {
    alert('Order details view will be implemented here for Order: ' + orderId);
}

function formatDate(dateString) {
    const options = { year: 'numeric', month: 'short', day: 'numeric' };
    return new Date(dateString).toLocaleDateString(undefined, options);
}
