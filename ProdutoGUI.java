import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.cell.PropertyValueFactory;
import java.util.List;
import java.sql.Connection;
import java.sql.SQLException;

public class ProdutoGUI extends Application {
	private ProdutoDAO produtoDAO;
	private ObservableList<Produto> produtos;
	private TableView<Produto> tableView;
	private TextField nomeInput, quantidadeInput, precoInput;
	private ComboBox<String> statusComboBox;
	private Connection conexaoDB;

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage palco) {
		conexaoDB = ConexaoDB.conectar();
		produtoDAO = new ProdutoDAO(conexaoDB); // inicializa o DAO 
		produtos = FXCollections.observableArrayList(produtoDAO.listarTodos()); // carrega todos os produtos do banco de dados

		palco.setTitle("Gerenciamento de Estoque de Produtos");

		VBox vbox = new VBox();
		vbox.setPadding(new Insets(10, 10, 10, 10)); // distancia entre o conteudo e as bordas em pixels
		vbox.setSpacing(10);

		HBox nomeProdutoBox = new HBox();
		nomeProdutoBox.setSpacing(10);
		Label nomeLabel = new Label("Produto: ");
		nomeInput = new TextField();
		nomeProdutoBox.getChildren().addAll(nomeLabel, nomeInput);

		HBox quantidadeBox = new HBox();
		quantidadeBox.setSpacing(10);
		Label quantidadeLabel = new Label("Quantidade: ");
		quantidadeInput = new TextField();
		quantidadeBox.getChildren().addAll(quantidadeLabel, quantidadeInput);

		HBox precoBox = new HBox();
		precoBox.setSpacing(10);
		Label precoLabel = new Label("Preco: ");
		precoInput = new TextField();
		precoBox.getChildren().addAll(precoLabel, precoInput);

		HBox statusBox = new HBox();
		statusBox.setSpacing(10);
		Label statusLabel = new Label("Status:");
		statusComboBox = new ComboBox<>();
		statusComboBox.getItems().addAll("Estoque Normal", "Estoque Baixo");
		statusBox.getChildren().addAll(statusLabel, statusComboBox);

		Button addButton = new Button("Adicionar");
		addButton.setOnAction(e -> {
			String preco = precoInput.getText().replace(',', '.'); // substitui virgula por ponto no preco;
			Produto produto = new Produto(nomeInput.getText(),
					Integer.parseInt(quantidadeInput.getText()),
					Double.parseDouble(preco),
					statusComboBox.getValue());
			produtoDAO.inserir(produto);
			produtos.setAll(produtoDAO.listarTodos());
			limparCampos(); // limpa os campos de entrada para uma nova digitacao;
		});

		Button updateButton = new Button("Atualizar");
		updateButton.setOnAction(e -> {
			Produto selectedProduto = tableView.getSelectionModel().getSelectedItem(); // obtem o produto selecionado
			if (selectedProduto != null) {
				selectedProduto.setNome(nomeInput.getText());
				selectedProduto.setQuantidade(Integer.parseInt(quantidadeInput.getText()));
				String preco = precoInput.getText().replace(',', '.');
				selectedProduto.setPreco(Double.parseDouble(preco));
				selectedProduto.setStatus(statusComboBox.getValue());
				produtoDAO.atualizar(selectedProduto);
				produtos.setAll(produtoDAO.listarTodos());
				limparCampos();
			}
		});

		Button deleteButton = new Button("Excluir");
		deleteButton.setOnAction(e -> {
			Produto selectedProduto = tableView.getSelectionModel().getSelectedItem();
			if (selectedProduto != null) {
				produtoDAO.excluir(selectedProduto.getId()); // excluir o produto do banco de dados
				produtos.setAll(produtoDAO.listarTodos());
				limparCampos();
			}
		});

		Button clearButton = new Button("Limpar");
		clearButton.setOnAction(e -> limparCampos());

		tableView = new TableView<>();
		tableView.setItems(produtos);
		tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS); // ajusta o tamanho das colunas para o mesmo tamanho
		List<TableColumn<Produto, ?>> columns = List.of(
				criarColuna("ID", "id"),
				criarColuna("Produto", "nome"),
				criarColuna("Quantidade", "quantidade"),
				criarColuna("Preço", "preco"),
				criarColuna("Status", "status")
		);
		tableView.getColumns().addAll(columns);

		tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
			if (newSelection != null) {
				nomeInput.setText(newSelection.getNome());
				quantidadeInput.setText(String.valueOf(newSelection.getQuantidade()));
				precoInput.setText(String.valueOf(newSelection.getPreco()));
				statusComboBox.setValue(newSelection.getStatus());
			}
		});

		HBox buttonBox = new HBox();
		buttonBox.setSpacing(10);
		buttonBox.getChildren().addAll(addButton, updateButton, deleteButton, clearButton); // adiciona os botoes ao hbox

		vbox.getChildren().addAll(nomeProdutoBox, quantidadeBox, precoBox, statusBox, buttonBox, tableView);

		Scene scene = new Scene(vbox, 800, 600);
		scene.getStylesheets().add("styles-produtos.css");
		palco.setScene(scene);
		palco.show();
	}

	/**
	 * o método stop é automaticamente chamado quando a aplicação JavaFX é encerrada
	 */

	@Override
	public void stop() {
		try {
			conexaoDB.close();
		} catch (SQLException e) {
			System.err.println("Erro ao fechar conexao." + e.getMessage());
		}
	}

	/**
	 * Limpa os campos de entrada do formulario
	 * Chamado apos adicionar, atualizar ou excluir um produto
	 * Garante que os campos de entrada estejam prontos para uma nova entrada
	 */

	private void limparCampos() {
		nomeInput.clear();
		quantidadeInput.clear();
		precoInput.clear();
		statusComboBox.setValue(null);
	}

	/**
	 * Cria uma coluna para a TableView
	 * @param title o título da coluna que será exibido no cabeçalho
	 * @param property propriedade do objeto Produto que esta coluna deve exibir
	 * @return a coluna configurada para a TableView
	 */

	private TableColumn<Produto, String> criarColuna(String title, String property) {
		TableColumn<Produto, String> col = new TableColumn<>(title);
		col.setCellValueFactory(new PropertyValueFactory<>(property)); // define a propriedade da coluna
		return col;
	}
}
