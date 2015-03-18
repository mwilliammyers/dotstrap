package server;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.*;

import server.database.*;
import shared.model.*;

/**
 * Imports data from an XML file to database
 */
public class Importer {
    //TODO: change the logging to non static throughout the project
    /**
     * Initializes the log.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private static void initLog() throws IOException {
        Level logLevel = Level.SEVERE;
        String logFile = "logs/importer.log";

        logger = Logger.getLogger("server");
        logger.setLevel(logLevel);
        logger.setUseParentHandlers(false);

        // Handler consoleHandler = new ConsoleHandler();
        // consoleHandler.setLevel(logLevel);
        // consoleHandler.setFormatter(new SimpleFormatter());
        // logger.addHandler(consoleHandler);

        // Set up 5 rolling logs each with a max file size of 3MB
        FileHandler fileHandler = new FileHandler(logFile, 3000000, 5, false);
        fileHandler.setLevel(logLevel);
        fileHandler.setFormatter(new SimpleFormatter());
        logger.addHandler(fileHandler);
    }

    private static Logger logger;
    static {
        try {
            initLog();
        } catch (IOException e) {
            System.out.println("Could not initialize log: " + e.getMessage());
        }
    }

    /**
     * The main method
     *
     * @param args the xml file to import into the database
     */
    public static void main(String[] args) {
        File xmlImportFile = new File(args[0]);
        File destImportDir = new File("Records");

        try {
            if (!xmlImportFile.getParentFile().getCanonicalPath()
                    .equals(destImportDir.getCanonicalPath())) {
                FileUtils.deleteDirectory(destImportDir);
            }

            FileUtils.copyDirectory(xmlImportFile.getParentFile(), destImportDir);
            Database.initDriver();

            Database db = new Database();
            db.startTransaction();
            db.initTables();
            db.endTransaction(true);

             File activeDB   = new File(Database.DB_FILE);
             File templateDB = new File(Database.DB_TEMPLATE);
             FileUtils.copyFile(activeDB, templateDB);

            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(xmlImportFile);
            doc.getDocumentElement().normalize();
            Element root = doc.getDocumentElement();
            new Importer().importData(root);

        } catch (Exception e) {
            logger.log(Level.SEVERE, e.toString());
            logger.log(Level.FINE, "STACKTRACE: ", e);
        }
        return;
    }

    private String getValue(Element elem) {
        String result = "";
        Node child = elem.getFirstChild();

        result = child.getNodeValue();

        return result;
    }

    /**
     * Checks if an Element contains a certain attribute
     *
     * @param elem
     *            the element to check for the attr
     * @param attr
     *            the attribute to check
     * @return true if elem > 0
     */
    private boolean contains(Element elem, String attr) {
        return elem.getElementsByTagName(attr).getLength() > 0;
    }

    /**
     * Gets the children elements of a Node
     */
    private ArrayList<Element> getChildElements(Node node) {
        ArrayList<Element> result = new ArrayList<Element>();
        NodeList children = node.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                result.add((Element) child);
            }
        }

        return result;
    }

    /**
     * Loads record indexer data into memory and the database
     * @param root
     */
    private void importData(Element root) {
        ArrayList<Element> rootElems = getChildElements(root);

        for (Element curr : getChildElements(rootElems.get(0))) {
            loadUsers(curr);
        }

        for (Element curr : getChildElements(rootElems.get(1))) {
            loadProjects(curr);
        }
    }

    /**
     * Inserts User element into database
     *
     * @param userElem
     */
    private void loadUsers(Element userElem) {
        //Get all User elements
        Element usernameElem = (Element)userElem.getElementsByTagName("username").item(0);
        Element passElem     = (Element)userElem.getElementsByTagName("password").item(0);
        Element firstElem    = (Element)userElem.getElementsByTagName("firstname").item(0);
        Element lastElem     = (Element)userElem.getElementsByTagName("lastname").item(0);
        Element emailElem    = (Element)userElem.getElementsByTagName("email").item(0);
        Element indexedElem  = (Element)userElem.getElementsByTagName("indexedrecords").item(0);

        //Get all User primitives from User Elements
        String username    = usernameElem.getTextContent();
        String password    = passElem.getTextContent();
        String firstName   = firstElem.getTextContent();
        String lastName    = lastElem.getTextContent();
        String email       = emailElem.getTextContent();
        int indexedRecords = Integer.parseInt(indexedElem.getTextContent());

        //Create new User and add it to the database
        Database db = new Database();
        try {
            db.startTransaction();

            User newUser = new User(-1, username, password, firstName, lastName, email, indexedRecords, 0);
            db.getUserDAO().create(newUser);

            db.endTransaction(true);
        } catch (DatabaseException e) {
            logger.log(Level.SEVERE, e.toString());
            logger.log(Level.FINE, "STACKTRACE: ", e);
        }
    }

    /**
     * Inserts a Project element into database
     */
    private void loadProjects(Element projectElem) {
        //Get Project Elements
        Element titleElem     = (Element)projectElem.getElementsByTagName("title").item(0);
        Element recPerImgElem = (Element)projectElem.getElementsByTagName("recordsperimage").item(0);
        Element firstYElem    = (Element)projectElem.getElementsByTagName("firstycoord").item(0);
        Element recordElem    = (Element)projectElem.getElementsByTagName("recordheight").item(0);

        //Get Project primitives from Project elements
        String title        = titleElem.getTextContent();
        int recordsPerImage = Integer.parseInt(recPerImgElem.getTextContent());
        int firstYCoord     = Integer.parseInt(firstYElem.getTextContent());
        int recordHeight    = Integer.parseInt(recordElem.getTextContent());

        int projectID = -1;
        Database db = new Database();
        //Create new project and add it to the database
        try {
            db.startTransaction();

            Project newProject = new Project(-1, title, recordsPerImage, firstYCoord, recordHeight);
            projectID = db.getProjectDAO().create(newProject);
            assert (projectID > 0);

            db.endTransaction(true);
        } catch (DatabaseException e) {
            logger.log(Level.SEVERE, e.toString());
            logger.log(Level.FINE, "STACKTRACE: ", e);
        }

        // Get project fields and images
        ArrayList<Element> children = getChildElements(projectElem);
        ArrayList<Element> fields   = getChildElements(children.get(4));
        ArrayList<Element> images   = getChildElements(children.get(5));

        // Add fields to database
        int colNum = 1;
        for (Element curr : fields) {
            loadFields(curr, projectID, colNum++);
        }
        // Add images to database
        for (Element curr : images) {
            loadBatches(curr, projectID);
        }
    }

    /**
     * Inserts a Field element into database
     */
    private void loadFields(Element fieldElem, int projectID, int colNum) {
        //Get Field elements
        Element titleElem  = (Element)fieldElem.getElementsByTagName("title").item(0);
        Element xCoordElem = (Element)fieldElem.getElementsByTagName("xcoord").item(0);
        Element knownDataElem  = (Element)fieldElem.getElementsByTagName("knowndata").item(0);
        Element helpElem   = (Element)fieldElem.getElementsByTagName("helphtml").item(0);
        Element widthElem  = (Element)fieldElem.getElementsByTagName("width").item(0);

        //Get Field primitives from Field elements
        String title     = titleElem.getTextContent();
        int xCoord       = Integer.parseInt(xCoordElem.getTextContent());
        String knownData = "";
        String helpHtml  = helpElem.getTextContent();
        int width        = Integer.parseInt(widthElem.getTextContent());

        if (knownDataElem != null) {
            knownData = "Records/" + knownDataElem.getTextContent();
        }

        Database db = new Database();
        try {
            db.startTransaction();

            Field newField = new Field(-1, projectID, title, knownData, helpHtml, xCoord, width, colNum);
            db.getFieldDAO().create(newField);

            db.endTransaction(true);
            db.endTransaction(true);
        } catch (DatabaseException e) {
            logger.log(Level.SEVERE, e.toString());
            logger.log(Level.FINE, "STACKTRACE: ", e);
        }
    }

    /**
     * Inserts an Image element into database
     */
    private void loadBatches(Element batchElem, int projectId) {
        //get file element
        Element batchFileElem = (Element)batchElem.getElementsByTagName("file").item(0);

        //get Batch primitive from batch file element
        String batchUrl = batchFileElem.getTextContent();

        int batchID = -1;
        ArrayList<Element> records = null;
        Database db = new Database();
        try {
            db.startTransaction();
            Batch newBatch = new Batch(batchUrl, projectId, Batch.INCOMPLETE, -1);
            batchID = db.getBatchDAO().create(newBatch);
            assert (batchID > 0);
            db.endTransaction(true);
        } catch (DatabaseException e) {
            logger.log(Level.SEVERE, e.toString());
            logger.log(Level.FINE, "STACKTRACE: ", e);
        }

        if (contains(batchElem, "records")) {
            ArrayList<Element> children = getChildElements(batchElem);
            records = getChildElements(children.get(1));

            int rowNum = 1;
            for (Element curr : records) {
                loadRecords(curr, projectId, batchID, batchUrl, rowNum++);
            }
        }
    }

    /**
     * Inserts a Record element into the database
     */
    private void loadRecords(Element recordElem, int projectID,
            int batchID, String batchUrl, int rowNum) {
        ArrayList<Element> children = getChildElements(recordElem);
        ArrayList<Element> records  = getChildElements(children.get(0));

        int colNum = 1;
        for (Element curr : records) {
            String recordData = getValue(curr);
            Database db = new Database();
            int fieldID = -1;
            try {
                db.startTransaction();
                fieldID = db.getFieldDAO().getFieldID(projectID, colNum);
                assert (fieldID > 0);
                Record newRecord = new Record(fieldID, batchID, batchUrl, recordData, rowNum, colNum);
                db.getRecordDAO().create(newRecord);
                db.endTransaction(true);
            } catch (DatabaseException e) {
                logger.log(Level.SEVERE, e.toString());
                logger.log(Level.FINE, "STACKTRACE: ", e);
            }
        }
    }
}
