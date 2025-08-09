package app;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Server {
	
	static final int BUF_SIZE = 50000000;
	static byte buf[] = new byte[BUF_SIZE];
	static byte buf2[] = new byte[BUF_SIZE];
	static int offset;
	static int offset2;

	static int server_port;
	static String target_host;
	static int target_port;
	
	static void perror(String msg) {
		System.out.println(msg);
	} 
	static void perrorexit(String msg) {
		perror(msg);
		System.exit(0);
	}

	//Supposition: Lis dans le buffer jusqu'à rencontrer un \r\n.
	static String read_line(InputStream is) {
		try {
			int c;
			int startOffset = 0;
			startOffset = offset2;
			while (true) {
				if (offset2 >= BUF_SIZE) perrorexit("_: read buffer overflow");
				c = is.read();
				if (c == -1) perrorexit("read_line: unexpected EOF");
				buf2[offset2++] = (byte)c;
				if (c == '\r') {
					if (offset2 >= BUF_SIZE) perrorexit("read_line: read buffer overflow");
					c = is.read();
					if (c == -1) perrorexit("read_line: unexpected EOF");
					buf2[offset2++] = (byte)c;
					if (c != '\n') perrorexit("read_line: incomplete CRLF sequence");
					int sz = 0;
					sz = offset2-startOffset-2;
					String ret = new String(buf2/*, startOffset, sz*/);
					System.out.println("read_line: received: "+ret);
					return ret;
				}
			}
		} catch (IOException e) {
			perrorexit("readline: IOException");
			return null;
		}
	}

	//Supposition: Lis le buffer/contenu de la réquête ou de la réponse
	static void read_buf(InputStream is, int len) {
		try {
			if ((BUF_SIZE-offset)<len) perrorexit("read_buf: read buffer overflow");
			while (len>0) {
				int ret = is.read(buf, offset, len);
				if (ret == -1) perrorexit("read_buf: read failed");
				//System.out.println("read_buf: received: "+ret+" bytes");
				offset += ret;
				len -= ret;
			}
		} catch (IOException e) {
			perrorexit("read_buf: IOException");
		}
	}
	
	static void write_buf(OutputStream os, byte buffer[], int len) {
		try {
			os.write(buffer, 0, len);
		} catch (IOException e) {
			perrorexit("write_buf: IOException");
		}
	}

	static int content_length(String line) {
	
		if (line.startsWith("Content-Length: ")) {
			int index = line.indexOf(" ")+1;
			String len = line.substring(index);
			try {
				return Integer.parseInt(len);
			} catch (Exception ex) {
				return 0;
			}
		}
		return 0;
	}
			
	static String get_request(String line) {
		int index = line.indexOf(" ");
		return line.substring(0, index);
	}
	
	static String get_return(String line) {
		int index1 = line.indexOf(" ")+1;
		int index2 = line.indexOf(" ", index1);
		return line.substring(index1, index2);
	}


	static String get_path(String line) {
		int index1 = line.indexOf(" ")+2;
		int index2 = line.indexOf(" ", index1);
		return line.substring(index1, index2);
	}


	static void handle_web(Socket cli) {

		try {
			int len, content_len, sz;
			String line, path, request;
			InputStream is;
			OutputStream os;

			offset = 0;
			is = cli.getInputStream();
			line = read_line(is);
			request = get_request(line);
			System.out.println("Request is "+request);
			path = get_path(line);

			content_len = 0;
			while (true) {
				line = read_line(is);
				len = content_length(line);
				if (len>0) content_len = len;
				if (line.length() == 0) break;
			}
			//System.out.println("Web request Content_Length: "+content_len);
			if (content_len>0) read_buf(is, content_len);

			os = cli.getOutputStream();
			if (request.equals("GET")) {
				System.out.println("Web request GET ("+path+")");
				try {
					sz = (int)new File(path).length();
					String path2 = "/home/sepia/Documents/ODBjv/bin/"+path;
					System.out.println("path passed to MyFileInputstream: "+path);
					FileInputStream fis = new FileInputStream(path);
					offset = 0;
					read_buf(fis, sz);
					String headers = "HTTP/1.0 200 OK\r\n";
					System.out.println("Web response: "+headers);
					byte headerb[] = headers.getBytes(StandardCharsets.US_ASCII);
					write_buf(os, headerb, headerb.length);
					headers = "Content-Length: "+sz+"\r\n\r\n";
					System.out.println("Web response: "+headers);
					headerb = headers.getBytes(StandardCharsets.US_ASCII);
					write_buf(os, headerb, headerb.length);
					write_buf(os, buf, sz);
					System.out.println("Web response: "+sz);
					cli.close();
					return;
				} catch (Exception ex) {
					perror("file not found");
					ex.printStackTrace();
					String headers = "HTTP/1.0 404 Not Found\r\n\r\n";
					byte headerb[] = headers.getBytes(StandardCharsets.US_ASCII);
					write_buf(os, headerb, headerb.length);
					System.out.println("Web response: not found\n");
					cli.close();
					return;
				}
			}
			perrorexit("Web request: unknown : "+request);
		} catch (IOException e) {
			perrorexit("handle_web: IOException");
		}
	}


	static void handle_inter(Socket cli) {

		try {
			int content_len, len;
			String line, path, request, ret;
			InputStream cis, sis;
			OutputStream cos, sos;

			offset = 0;
			cis = cli.getInputStream();
			line = read_line(cis);
			request = get_request(line);
			path = get_path(line);

			content_len = 0;
			//Supposition: Bout de code pour vérifier si l'IS du Socket a été complètement lu.
			while (true) {
				line = read_line(cis);
				len = content_length(line);
				System.out.println("line " + line);
				if (len>0) content_len = len;
				if (line.length() == 0) break;
			}
			System.out.println("Web request Content_Length: "+content_len);
			//Supposition: Si la requête/réponse a une payload, on la lit.
			if (content_len>0) read_buf(cis, content_len);

			//Supposition: On récupère l'OS du Socket courant
			cos = cli.getOutputStream();
			if (request.equals("GET")) {
				System.out.println("Web request GET ("+path+")");
				try {
					//On se connecte à la target et on récupère son OS et son IS.
					Socket serv = new Socket(target_host, target_port);
					sis = serv.getInputStream();
					sos = serv.getOutputStream();

					//Supposition: On écrit dans l'OS de la target à laquelle on vient de se connecter
					write_buf(sos, buf, offset);
					System.out.println("Inter request Have written: "+offset);

					offset = 0;
					//Supposition: On lit la réponse envoyée par le target
					line = read_line(sis);
					ret = get_return(line);
					System.out.println("Inter response: "+ret);

					//Supposition: On vérifie s'il y'a unne payload
					content_len = 0;
					while (true) {
						line = read_line(sis);
						len = content_length(line);
						if (len>0) content_len = len;
						if (line.length() == 0) break;
					}
					//System.out.println("Inter response Content_Length: "+content_len);
					//Supposition: Si (payload), on la récupère et on ferme la connexion avec le target.
					if (content_len>0) read_buf(sis, content_len);
					serv.close();
					System.out.println("----------------INTER WRITE-----------");
					//On réécrit dans l'OS du Socket du serveur courant, cet OS pointe vers nous, le client
					write_buf(cos, buf, offset);
					System.out.println("Inter response: "+offset);
					//On ferma la connexion avec le client
					cli.close();
					return;
				} catch (Exception ex) {
					perror("file not found");
					ex.printStackTrace();
					String headers = "HTTP/1.0 404 Not Found\r\n\r\n";
					byte headerb[] = headers.getBytes(StandardCharsets.US_ASCII);
					write_buf(cos, headerb, headerb.length);
					//System.out.println("Web response: not found\n");
					cli.close();
					return;
				}
			}
			perrorexit("Web request: unknown : "+request);
		} catch (IOException e) {
			perrorexit("handle_inter: IOException");
		}
	}

	static enum Role {UNKNOWN, WEB, INTER, LB};

	public static void main(String args[]) {

		try {
			Role role = Role.UNKNOWN;

			// java Server web|inter|lb <server_port> <target_host> <target_port>

			if (args.length < 2) perrorexit("main: bad args number");
			server_port = Integer.parseInt(args[1]);

			switch (args[0]) {
			case "web":
				System.out.println("web server");
				role = Role.WEB; break;
			case "inter":
				System.out.println("intermediate server");
				role=Role.INTER;
				if (args.length != 4) perrorexit("main: bad args number");
				target_host = args[2];
				target_port = Integer.parseInt(args[3]);
				break;
			case "lb":
				System.out.println("load balancing server");
				role=Role.LB;
				if (args.length != 4) perrorexit("main: bad args number");
				target_host = args[2];
				target_port = Integer.parseInt(args[3]);
				break;
			}

			ServerSocket ss = new ServerSocket(server_port);

			while (true) {
				//System.out.println("before accept");
				Socket cli = ss.accept();
				//System.out.println("got connection");

				switch (role) {
				case WEB: handle_web(cli); break;
				case INTER: handle_inter(cli); break;
				case LB: handle_inter(cli); break;
				}
				//System.out.println("close connection\n");
			}
		} catch (NumberFormatException e) {
			perrorexit("main: bad args");
		} catch (IOException e) {
			perrorexit("main: IOException");
		}
	}
}




