$(document).ready(function() {
    loadCustomers();
    getNextCustomerId();
    $('#updateCustomerBtn').hide();

    $('#saveCustomerBtn').click(function(e) {
        e.preventDefault();
        const customerData = {
            id: $('#customerId').val(),
            name: $('#customerName').val(),
            email: $('#customerEmail').val(),
            contact: $('#customerPhone').val(),
            address: $('#customerAddress').val()
        };

        if (!customerData.id || !customerData.name || !customerData.email || !customerData.contact || !customerData.address) {
            alert('Please fill in all required fields');
            return;
        }

        $.ajax({
            url: 'http://localhost:8081/Appication_1_Web_exploded/pages/customer',
            method: 'POST',
            data: customerData,
            success: function(response) {
                alert('Customer saved successfully');
                clearForm();
                loadCustomers();
            },
            error: function(xhr, status, error) {
                console.error('Error saving customer:', error);
                alert('Error saving customer check your email and phone number if already exist');
            }
        });
    });

    $('#updateCustomerBtn').click(function() {
        const customerData = new URLSearchParams({
            id: $('#customerId').val(),
            name: $('#customerName').val(),
            email: $('#customerEmail').val(),
            contact: $('#customerPhone').val(),
            address: $('#customerAddress').val()
        }).toString();

        $.ajax({
            url: 'http://localhost:8081/Appication_1_Web_exploded/pages/customer?' + customerData,
            type: 'PUT',
            success: function(response) {
                alert('Customer updated successfully');
                clearForm();
                loadCustomers();
                $('#saveCustomerBtn').show();
                $('#updateCustomerBtn').hide();
            },
            error: function(xhr, status, error) {
                console.error('Error updating customer:', error);
                alert('Error updating customer');
            }
        });
    });

    $('.btn-danger').click(function() {
        clearForm();
    });
});

function loadCustomers() {
    $.ajax({
        url: 'http://localhost:8081/Appication_1_Web_exploded/pages/customer',
        method: 'GET',
        success: function(response) {
            displayCustomers(response);
        },
        error: function(xhr, status, error) {
            console.error('Error loading customers:', error);
            alert('Error loading customers');
        }
    });
}

function getNextCustomerId() {
    $.ajax({
        url: 'http://localhost:8081/Appication_1_Web_exploded/pages/customer',
        method: 'GET',
        data: { option: 'GETID' },
        success: function(response) {
            $('#customerId').val(response.id);
            $('#customerId').prop('readonly', true);
        },
        error: function(xhr, status, error) {
            console.error('Error getting next ID:', error);
            alert('Error getting next ID');
        }
    });
}

function displayCustomers(customers) {
    const tbody = $('#customerTableBody');
    tbody.empty();

    customers.forEach(customer => {
        tbody.append(`
            <tr>
                <td>${customer.id}</td>
                <td>${customer.name}</td>
                <td>${customer.email}</td>
                <td>${customer.contact}</td>
                <td>${customer.address}</td>
                <td>
                    <button class="btn action-btn btn-edit" id="editBtn-${customer.id}">Edit</button>
                    <button class="btn action-btn btn-delete" id="deleteBtn-${customer.id}">Delete</button>
                </td>
            </tr>
        `);

        $(`#deleteBtn-${customer.id}`).click(function() {
            if (confirm('Are you sure you want to delete this customer?')) {
                $.ajax({
                    url: 'http://localhost:8081/Appication_1_Web_exploded/pages/customer?id=' + customer.id,
                    type: 'DELETE',
                    success: function(response) {
                        alert('Customer deleted successfully');
                        getNextCustomerId();
                        loadCustomers();
                    },
                    error: function(xhr, status, error) {
                        console.error('Error deleting customer:', error);
                        alert('Error deleting customer');
                    }
                });
            }
        });

        $(`#editBtn-${customer.id}`).click(function() {
            $('#customerId').val(customer.id);
            $('#customerName').val(customer.name);
            $('#customerEmail').val(customer.email);
            $('#customerPhone').val(customer.contact);
            $('#customerAddress').val(customer.address);

            $('#saveCustomerBtn').hide();
            $('#updateCustomerBtn').show();
        });
    });
}

function clearForm() {
    $('#customerId').val('');
    $('#customerName').val('');
    $('#customerEmail').val('');
    $('#customerPhone').val('');
    $('#customerAddress').val('');
    getNextCustomerId();
    $('#saveCustomerBtn').show();
    $('#updateCustomerBtn').hide();
}

function clearOnActon() {
    $('#saveCustomerBtn').show();
    $('#updateCustomerBtn').hide();
    clearForm();
}