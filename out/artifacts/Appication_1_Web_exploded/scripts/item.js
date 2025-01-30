$(document).ready(function() {
    loadItems();
    getNextItemId();
    $('#updateItemBtn').hide();

    $('#saveItemBtn').click(function(e) {
        e.preventDefault();
        const itemData = {
            id: $('#itemId').val(),
            name: $('#itemName').val(),
            price: $('#price').val(),
            stock: $('#stockQuantity').val(),
            category: $('#category').val()
        };

        if (!itemData.id || !itemData.name || !itemData.price || !itemData.stock || !itemData.category) {
            alert('Please fill in all required fields');
            return;
        }

        $.ajax({
            url: 'http://localhost:8081/Appication_1_Web_exploded/pages/item',
            method: 'POST',
            data: itemData,
            success: function(response) {
                alert('Item saved successfully');
                clearForm();
                loadItems();
            },
            error: function(xhr, status, error) {
                console.error('Error saving item:', error);
                alert('Error saving item');
            }
        });
    });

    $('#updateItemBtn').click(function() {
        const itemData = new URLSearchParams({
            id: $('#itemId').val(),
            name: $('#itemName').val(),
            price: $('#price').val(),
            stock: $('#stockQuantity').val(),
            category: $('#category').val()
        }).toString();

        $.ajax({
            url: 'http://localhost:8081/Appication_1_Web_exploded/pages/item?' + itemData,
            type: 'PUT',
            success: function(response) {
                alert('Item updated successfully');
                clearForm();
                loadItems();
                $('#saveItemBtn').show();
                $('#updateItemBtn').hide();
            },
            error: function(xhr, status, error) {
                console.error('Error updating item:', error);
                alert('Error updating item');
            }
        });
    });

    $('#clearBtn').click(function() {
        clearForm();
    });
});

function loadItems() {
    $.ajax({
        url: 'http://localhost:8081/Appication_1_Web_exploded/pages/item',
        method: 'GET',
        success: function(response) {
            displayItems(response);
        },
        error: function(xhr, status, error) {
            console.error('Error loading items:', error);
            alert('Error loading items');
        }
    });
}

function getNextItemId() {
    $.ajax({
        url: 'http://localhost:8081/Appication_1_Web_exploded/pages/item',
        method: 'GET',
        data: { option: 'GETID' },
        success: function(response) {
            $('#itemId').val(response.id);
            $('#itemId').prop('readonly', true);
        },
        error: function(xhr, status, error) {
            console.error('Error getting next ID:', error);
            alert('Error getting next ID');
        }
    });
}

function displayItems(items) {
    const tbody = $('#itemTableBody');
    tbody.empty();

    items.forEach(item => {
        tbody.append(`
            <tr>
                <td>${item.id}</td>
                <td>${item.name}</td>
                <td>$${item.price.toFixed(2)}</td>
                <td>${item.stock}</td>
                <td>${item.category}</td>
                <td>
                    <button class="btn btn-primary btn-action" id="editBtn-${item.id}">Edit</button>
                    <button class="btn btn-danger btn-action" id="deleteBtn-${item.id}">Delete</button>
                </td>
            </tr>
        `);

        $(`#deleteBtn-${item.id}`).click(function() {
            if (confirm('Are you sure you want to delete this item?')) {
                $.ajax({
                    url: 'http://localhost:8081/Appication_1_Web_exploded/pages/item?id=' + item.id,
                    type: 'DELETE',
                    success: function(response) {
                        alert('Item deleted successfully');
                        getNextItemId();
                        loadItems();
                    },
                    error: function(xhr, status, error) {
                        console.error('Error deleting item:', error);
                        alert('Error deleting item');
                    }
                });
            }
        });

        $(`#editBtn-${item.id}`).click(function() {
            $('#itemId').val(item.id);
            $('#itemName').val(item.name);
            $('#price').val(item.price);
            $('#stockQuantity').val(item.stock);
            $('#category').val(item.category);

            $('#saveItemBtn').hide();
            $('#updateItemBtn').show();
        });
    });
}

function clearForm() {
    $('#itemId').val('');
    $('#itemName').val('');
    $('#price').val('');
    $('#stockQuantity').val('');
    $('#category').val('');
    getNextItemId();
    $('#saveItemBtn').show();
    $('#updateItemBtn').hide();
}
