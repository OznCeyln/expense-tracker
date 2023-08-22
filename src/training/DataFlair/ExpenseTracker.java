package training.DataFlair;

import java.awt.EventQueue;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import org.sqlite.SQLiteDataSource;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JComboBox;
import javax.swing.table.DefaultTableModel;
import javax.swing.JTextField;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class ExpenseTracker {
	private static Connection conn;
	private static SQLiteDataSource ds;
	private JFrame frame;
	private JTable table;
	private JTextField dateField;
	private JTextField descField;
	private JTextField amountField;
	private JTextField nameField;
	private int currentAccountId =0;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ExpenseTracker window = new ExpenseTracker();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public ExpenseTracker() {
		initDB();
		initialize();
	}

//	Method to initialize the database and create tables if they do no exist
	private void initDB() {
ds = new SQLiteDataSource();
		
		try {
            ds = new SQLiteDataSource();
            ds.setUrl("jdbc:sqlite:ExpensesDB.db");
        } catch ( Exception e ) {
            e.printStackTrace();
            
            System.exit(0);
        }
        try {
        	 conn = ds.getConnection();
        	 
        	 Statement statement = conn.createStatement();
             statement.executeUpdate("CREATE TABLE IF NOT EXISTS accounts (\n"
             		+ "    id INTEGER PRIMARY KEY AUTOINCREMENT,\n"
             		+ "    name TEXT\n"
             		+ ");\n"
             		+ "CREATE TABLE IF NOT EXISTS expenses (\n"
             		+ "    id INTEGER PRIMARY KEY AUTOINCREMENT,\n"
             		+ "    account_id INTEGER,\n"
             		+ "    date TEXT,\n"
             		+ "    description TEXT,\n"
             		+ "    amount REAL,\n"
             		+ "    FOREIGN KEY (account_id) REFERENCES accounts(id)\n"
             		+ ");\n"
             		);
            
//           Closing statement and connection  
             statement.close();
        	 conn.close();
        	 
        }catch ( SQLException e ) {
            e.printStackTrace();
            System.exit( 0 );
        }
        finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            }catch (SQLException e) {
                System.err.println(e);
              }
        
        }

	}
//Method to add account to the database
	private void addAccount(String accountName) {
        try {
            // Open connection to the database
        	conn = ds.getConnection();

            // Prepare SQL statement for inserting data into the accounts table
            String sql = "INSERT INTO accounts (name) VALUES (?)";
            PreparedStatement stmt = conn.prepareStatement(sql);

            // Set parameter for the statement
            stmt.setString(1, accountName);

            // Execute the statement to insert the data into the table
            stmt.executeUpdate();

            // Close the statement and connection
            stmt.close();
            conn.close();
            
            JOptionPane.showMessageDialog(frame, "Account Added Successfully");
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame,"Error adding account: " + e.getMessage());
        }
    }
	
//	Method to Load Data from the database into the table
	public void loadData(DefaultTableModel model,int acId) throws SQLException {
		model.setRowCount(0);
		conn = ds.getConnection();
		String sql = "SELECT date,description,amount FROM expenses WHERE account_id = ? ;";
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setInt(1, acId);
		ResultSet rs = ps.executeQuery();
		Object[] row = new Object[3]; 
		while (rs.next()) {
			for (int i = 0; i < row.length; i++) {
				row[i] = rs.getObject(i+1);
			}
			model.addRow(row);

		}
		ps.close();
		conn.close();
	}
	
//	method to get account details such as account name and total expense from the database
	private String[] getAccountDetails(int accountId) {
		double totalExpense = 0.0;
        String accountName = null;
		try {
        	conn = ds.getConnection();
            

            // Prepare SQL statement to retrieve account name
            String accountSql = "SELECT name FROM accounts WHERE id = ?";
            PreparedStatement accountStmt = conn.prepareStatement(accountSql);
            accountStmt.setInt(1, accountId);

            // Execute the account statement and retrieve the result
            ResultSet accountResult = accountStmt.executeQuery();
            if (accountResult.next()) {
                accountName = accountResult.getString("name");
                
                // Prepare SQL statement to calculate total expense amount
                String expenseSql = "SELECT SUM(amount) AS total FROM expenses WHERE account_id = ?";
                PreparedStatement expenseStmt = conn.prepareStatement(expenseSql);
                expenseStmt.setInt(1, accountId);

                // Execute the expense statement and retrieve the result
                ResultSet expenseResult = expenseStmt.executeQuery();
                if (expenseResult.next()) {
                    totalExpense = expenseResult.getDouble("total");
                    }
                    
                expenseResult.close();
                expenseStmt.close();
            } 
            accountResult.close();
            accountStmt.close();
            conn.close();
        } catch (SQLException e) {
        	JOptionPane.showMessageDialog(frame,"Error retrieving account details: " + e.getMessage());
        }
        String[] detail = {accountName,totalExpense+""};
        return detail;

    }
	
//	Method to add the expense into the currently selected account 
    private void addExpense(int accountId, String date, String description, double amount) {
        try {
            // Open connection to the database
            conn = ds.getConnection();

            // Prepare SQL statement for inserting data into the expenses table
            String sql = "INSERT INTO expenses (account_id, date, description, amount) VALUES (?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);

            // Set parameters for the statement
            stmt.setInt(1, accountId);
            stmt.setString(2, date);
            stmt.setString(3, description);
            stmt.setDouble(4, amount);

            // Execute the statement to insert the data into the table
            stmt.executeUpdate();

            // Close the statement and connection
            stmt.close();
            conn.close();

            JOptionPane.showMessageDialog(frame,"Expense added successfully to Account ID: " + accountId);
        } catch (SQLException e) {
        	JOptionPane.showMessageDialog(frame,"Error adding expense: " + e.getMessage());
        }
    }

	
//	Method to update the comboBox with data from the database
	public void updateCombox(JComboBox<String> cbx) throws SQLException {
		cbx.removeAll();
		conn = ds.getConnection();
		String sql = "SELECT * FROM accounts;";
		PreparedStatement ps = conn.prepareStatement(sql);
		ResultSet rs = ps.executeQuery();
		
		
		while(rs.next()) {
			cbx.addItem(rs.getString("id") +"|"+ rs.getString("name"));
		}
		rs.close();
		ps.close();
		conn.close();

	}
	
	
	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100,600, 400);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		frame.setTitle("Expense Tracker by DataFlair");
		
		JPanel toppanel = new JPanel();
		toppanel.setBounds(0, 0, 590, 58);
		frame.getContentPane().add(toppanel);
		toppanel.setLayout(null);
		
		JLabel lblSelectAc = new JLabel("Select A/C:");
		lblSelectAc.setBounds(0, 0, 75, 15);
		toppanel.add(lblSelectAc);
		
		JComboBox accBox = new JComboBox();
		accBox.setBounds(86, 0, 130, 24);
		toppanel.add(accBox);
		
		JLabel lblName = new JLabel("Name:");
		lblName.setBounds(10, 37, 70, 15);
		toppanel.add(lblName);
		
		nameField = new JTextField();
		nameField.setColumns(10);
		nameField.setBounds(86, 27, 130, 30);
		toppanel.add(nameField);
		
		JButton btnAddAc = new JButton("Add A/C");
		btnAddAc.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addAccount(nameField.getText());
				try {
					updateCombox(accBox);
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		btnAddAc.setBounds(223, 27, 117, 30);
		toppanel.add(btnAddAc);
		
		JButton btnSelect = new JButton("Select");
		btnSelect.setBounds(223, 0, 117, 25);
		toppanel.add(btnSelect);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setEnabled(false);
		scrollPane.setBounds(0, 60, 590, 211);
		frame.getContentPane().add(scrollPane);
		
		table = new JTable();
		table.setBounds(0, 0, 0, 0);
		table.setModel(new DefaultTableModel(
			new Object[][] {
				{null, null, null},
			},
			new String[] {
				"Date", "Description", "Amount"
			}
		));
		scrollPane.setViewportView(table);
		
		JPanel bottomPanel = new JPanel();
		bottomPanel.setBounds(0, 270, 600, 90);
		frame.getContentPane().add(bottomPanel);
		bottomPanel.setLayout(null);
		
		JLabel dateLabel = new JLabel("Date:");
		dateLabel.setBounds(0, 5, 50, 15);
		bottomPanel.add(dateLabel);
		
		dateField = new JTextField();
		dateField.setBounds(50, 5, 114, 30);
		bottomPanel.add(dateField);
		dateField.setColumns(10);
		
		JLabel descLabel = new JLabel("Description:");
		descLabel.setBounds(180, 5, 90, 15);
		bottomPanel.add(descLabel);
		
		descField = new JTextField();
		descField.setColumns(10);
		descField.setBounds(270, 5, 114, 30);
		bottomPanel.add(descField);
		
		JLabel amountLabel = new JLabel("Amount:");
		amountLabel.setBounds(390, 5, 70, 15);
		bottomPanel.add(amountLabel);
		
		amountField = new JTextField();
		amountField.setColumns(10);
		amountField.setBounds(456, 5, 114, 30);
		bottomPanel.add(amountField);
		
		JButton btnAddExpense = new JButton("Add");
		btnAddExpense.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addExpense(currentAccountId, dateField.getText(), descField.getText(),Double.valueOf(amountField.getText()) );
				try {
					loadData((DefaultTableModel)table.getModel(), currentAccountId);
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		btnAddExpense.setBounds(239, 39, 117, 25);
		bottomPanel.add(btnAddExpense);
		
		JLabel lblTotalExpense = new JLabel("Total Expense : ");
		lblTotalExpense.setBounds(22, 75, 120, 15);
		bottomPanel.add(lblTotalExpense);
		
		JLabel lbltotalAmount = new JLabel("--");
		lbltotalAmount.setBounds(143, 75, 70, 15);
		bottomPanel.add(lbltotalAmount);
		
		JLabel lblCurrAcc = new JLabel("Current Acc Name:");
		lblCurrAcc.setBounds(270, 76, 130, 15);
		bottomPanel.add(lblCurrAcc);
		
		JLabel lblAccountName = new JLabel("Account Name");
		lblAccountName.setBounds(412, 76, 130, 15);
		bottomPanel.add(lblAccountName);
		
		btnSelect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String accountId = (String)accBox.getSelectedItem();
				accountId = accountId.substring(0, accountId.indexOf('|'));
				currentAccountId = Integer.valueOf(accountId);
				try {
					loadData((DefaultTableModel)table.getModel(), currentAccountId);
				} catch (NumberFormatException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				String details[] = getAccountDetails(currentAccountId);
				lbltotalAmount.setText(details[1]);
				lblAccountName.setText(details[0]);
			}
			
		});
		
		try {
			updateCombox(accBox);
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
}
