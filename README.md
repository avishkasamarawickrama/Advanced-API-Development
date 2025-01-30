# Servlet Project

This project is a collection of Java servlets that demonstrate various functionalities and configurations in the **Jakarta Servlet API**. It includes examples of handling HTTP methods, URL mappings, and generating dynamic HTML responses.

---

## **Features**
- **HelloServlet**: Handles HTTP methods like GET, POST, PUT, DELETE, and OPTIONS.
- **ExtensionMapping**: Demonstrates extension-based URL patterns (e.g., `*.json`).
- **MultipleMapping**: Shows how to map multiple URL patterns to a single servlet.
- **TestDeploymentDServlet**: A basic servlet for testing deployment.
- **TestServlet**: Generates an HTML response with inline CSS.

---

## **Technologies Used**
- Java
- Jakarta Servlet API (Jakarta EE)
- Maven (for dependency management)
- Git (for version control)

---

## **How to Run the Project**
1. **Clone the Repository**:
   ```bash
   git clone https://github.com/avishkasamarawickrama/Advanced-API-Development.git

2. ## **Navigate to the Project Directory**

 cd servlet-project

3. ## **Build the Project:**
mvn clean install

4.**Deploy the Application**

Deploy the generated WAR file to a servlet container like Apache Tomcat.

5.**Access the Servlets**

**Open a browser and navigate to the appropriate URLs:**

1./hello for HelloServlet

2./test for TestServlet

3./yy or /zz for MultipleMapping

4.Any URL ending with .json for ExtensionMapping

**Project Structure**

![Screenshot 2025-01-30 115054](https://github.com/user-attachments/assets/93b08b20-07ae-4767-8de5-143accd34043)






 
