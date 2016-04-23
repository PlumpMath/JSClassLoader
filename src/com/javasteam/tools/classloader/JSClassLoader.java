package com.javasteam.tools.classloader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.Manifest;

public class JSClassLoader extends URLClassLoader {
	public JSClassLoader() {
		super( new URL[ 0 ], null );
	}

	public JSClassLoader( ClassLoader parent, boolean doLoad ) {
		super( new URL[ 0 ], parent );

  	String path = System.getenv( "JSLIBDIR" );
  	
  	if( path != null ) {
  		try {
  			System.out.println( "Loading from " + path );
				addJarsFromDirectory( path, true );
			}
			catch( MalformedURLException e ) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
  	}
	}
	
	public JSClassLoader( ClassLoader parent ) {
		this( parent, true );
	}

	public JSClassLoader( URL url ) throws MalformedURLException {
		super( new URL[] { url }, null );
	}

	public JSClassLoader( URL url, ClassLoader parent ) throws MalformedURLException {
		super( new URL[] { url }, parent );
	}

	public JSClassLoader( File file, ClassLoader parent ) throws MalformedURLException {
		this( file.toURI().toURL(), parent );
	}

	public JSClassLoader( File file ) throws MalformedURLException {
		this( file.toURI().toURL(), null );
	}

	public JSClassLoader( String path, ClassLoader parent ) throws MalformedURLException {
		this( new URL( "file:" + path ), parent );
	}

	public JSClassLoader( String path ) throws MalformedURLException {
		this( new URL( "file:" + path ), null );
	}

	/*
	 * private byte[] loadClassBytes( String name ) throws IOException {
	 * InputStream stream = getClass().getClassLoader().getResourceAsStream( name
	 * ); int length = stream.available(); byte bytes[] = new byte[ length ];
	 * 
	 * DataInputStream in = new DataInputStream( stream ); in.readFully( bytes );
	 * in.close();
	 * 
	 * return bytes; }
	 */

	private void addDirectory( String dir ) {
		try {
			String manifestPath = dir + File.separator + "META-INF" + File.separator + "MANIFEST.MF";

			if(( new File( manifestPath )).exists() ) {
				try {
					FileInputStream fileInputStream = new FileInputStream( manifestPath );
					Manifest manifest = new java.util.jar.Manifest( fileInputStream );
					fileInputStream.close();

					FileReader fileReader = new FileReader( manifestPath );
					BufferedReader bufferedReader = new BufferedReader( fileReader );
					String line = bufferedReader.readLine();

					while( line != null ) {
						if( line.length() > 6 && line.toLowerCase().startsWith( "name:" ) ) {
							String temp = line.substring( "name: ".length() );
							int lastDot = temp.lastIndexOf( "." );

							if( lastDot != -1 ) {
								String packageName = temp.substring( 0, lastDot );
								URL theUrl = new URL( "file", "", dir );

								definePackage( packageName, manifest, theUrl );
							}
						}

						line = bufferedReader.readLine();
					}

					bufferedReader.close();
					fileReader.close();
				}
				catch( FileNotFoundException e ) {
					// TODO Auto-generated catch block
					// e.printStackTrace();
				}
				catch( IOException e ) {
					// TODO Auto-generated catch block
					// e.printStackTrace();
				}
			}
		}
		catch( Throwable t ) {
			t.printStackTrace();
			// throw( t );
		}
	}

	public synchronized Class<?> loadClass( String className ) throws ClassNotFoundException {
		Class<?> theClass = findLoadedClass( className );

		if( theClass == null ) {
  		theClass = super.loadClass( className );

	  	if( theClass == null ) {
			  if( getParent() != null ) {
						theClass = getParent().loadClass( className );
					}
					// If no parent is specified, let the System class loader find the
					// class.
					else if( this != System.class.getClassLoader() ) {
						theClass = System.class.getClassLoader().loadClass( className );
					}
				}

				if( theClass == null ) {
					theClass = super.loadClass( className );
				}
			}

		return theClass;
	}

	public URL findResourceFile( String name ) {
		URL[] urls = this.getURLs();
		URL theUrl = null;

		if( urls != null ) {
			for( int i = 0; i < urls.length && theUrl == null; ++i ) {
				File f = new File( urls[ i ].getPath() );

				if( f.isDirectory() ) {
					String propertyFileName = urls[ i ].getPath() + "/" + name;
					File target = new File( propertyFileName );

					if( target.exists() ) {
						try {
							theUrl = new URL( propertyFileName );
						}
						catch( IOException e ) {
							// System.out.println( "Resource load failed: " + e.getMessage()
							// );
						}
					}
				}
			}
		}

		return theUrl;
	}

	public URL findResource( String resourceName ) {
		URL resourceUrl = findResourceFile( resourceName );

		if( resourceUrl == null ) {
			resourceUrl = super.findResource( resourceName );
		}

		return resourceUrl;
	}

	public String getPath() {
		URL[] urls = this.getURLs();
		StringBuffer result = new StringBuffer();

		if( urls != null ) {
			for( int i = 0; i < urls.length; ++i ) {
				if( i > 0 ) {
					result.append( File.pathSeparatorChar );
				}

				result.append( urls[ i ].toString() );
			}
		}

		return result.toString();
	}

	public String getClassPath() {
		URL[] urls = this.getURLs();
		StringBuffer thePath = new StringBuffer();

		if( urls != null ) {
			for( int i = 0; i < urls.length; ++i ) {
				if( i > 0 ) {
					thePath.append( File.pathSeparatorChar );
				}

				thePath.append( urls[ i ].getPath() );
			}
		}

		return thePath.toString();
	}

	public void addJar( File theFile ) throws MalformedURLException {
		String fileURL = theFile.getAbsolutePath();

		if( theFile.isDirectory() ) {
			addDirectory( fileURL );
		}
		//else {
		addURL( new URL( "file", "", fileURL ) );
		//}
	}

	public void addJar( String file ) throws MalformedURLException {
		addJar( new File( file ) );
	}

	public void addJars( File[] jarfiles ) throws MalformedURLException {
		for( int i = 0; i < jarfiles.length; ++i ) {
			addJar( jarfiles[ i ] );
		}
	}
	
	public void addJarsFromDirectory( String path, Boolean recurse ) throws MalformedURLException {
    addURL( new URL( "file", "", path ));

    File   file  = new File( path );
    File[] files = file.listFiles();

    if( files != null ) {
      for( int i = 0; i < files.length; ++i ) {
        if( files[i].isDirectory() && recurse.booleanValue() ) {
          addJarsFromDirectory( files[i].getPath(), recurse );
        }

        if( files[i].getName().endsWith( ".jar" ) && !files[i].isDirectory() ) {
          addJar( files[i] );
        }
      }
    }
  }

}
